package application.servicies;

import application.models.Forum;
import application.models.Post;
import application.models.Thread;
import application.models.User;
import application.views.PostFullResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;


@Service
@Transactional
public class PostService {
    private final NamedParameterJdbcTemplate template;
    private final UserService userService;
    private final ThreadService threadService;


    @Autowired
    public PostService(NamedParameterJdbcTemplate template,
                       UserService userService,
                       ThreadService threadService) {
        this.template = template;
        this.userService = userService;
        this.threadService = threadService;
    }


    private static final RowMapper<Post> POST_MAPPER = (res, num) -> new Post(
            res.getLong("id"),
            res.getString("author"),
            res.getLong("author_id"),
            res.getTimestamp("created"),
            res.getString("forum"),
            res.getBoolean("is_edited"),
            res.getString("message"),
            res.getLong("parent"),
            res.getLong("thread_id")
    );


    private static final RowMapper<PostFullResponse> POST_FULL_MAPPER = (res, num) -> new PostFullResponse(
            new User(
                    res.getLong("user_id"),
                    res.getString("user_nickname"),
                    res.getString("user_fullname"),
                    res.getString("user_email"),
                    res.getString("user_about")
            ),
            new Forum(
                    res.getLong("forum_id"),
                    res.getString("forum_slug"),
                    res.getString("forum_title"),
                    res.getString("forum_user"),
                    res.getLong("forum_person_id"),
                    res.getLong("forum_posts"),
                    res.getLong("forum_threads")
            ),
            new Post(
                    res.getLong("post_id"),
                    res.getString("user_nickname"),
                    res.getLong("user_id"),
                    res.getTimestamp("post_created"),
                    res.getString("forum_slug"),
                    res.getBoolean("post_is_edited"),
                    res.getString("post_message"),
                    res.getLong("post_parent"),
                    res.getLong("thread_id")
            ),
            new Thread(
                    res.getLong("thread_id"),
                    res.getString("thread_author"),
                    res.getLong("thread_author_id"),
                    res.getTimestamp("thread_created"),
                    res.getString("forum_slug"),
                    res.getLong("forum_id"),
                    res.getString("thread_message"),
                    res.getString("thread_slug"),
                    res.getString("thread_title"),
                    res.getLong("thread_votes")
            )
    );


    // Добавление новых постов в ветку обсуждения на форум.
    // Все посты, созданные в рамках одного вызова данного метода должны иметь одинаковую дату создания (Post.Created).
    public List<Post> createPosts(String slugOrId, List<Post> body) throws IndexOutOfBoundsException, NoParentPostException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);     // Может выпасть IndexOutOfBoundsException - ветка не найдена

        if (!isPostsHasParents(thread, body)) {
            throw new NoParentPostException();
        }

        final GeneratedKeyHolder keys = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        final StringBuilder values = new StringBuilder();
        final List<User> authors = new ArrayList<>();
        for (Integer i = 0; i < body.size(); ++i) {
            final User author = userService.findUserByNickname(body.get(i).getAuthor());   // Может выпасть IndexOutOfBoundsException - автор не найден
            authors.add(author);

            params.addValue("author_id_" + i, author.getId());
            params.addValue("thread_id_" + i, thread.getId());
            params.addValue("message_" + i, body.get(i).getMessage());
            params.addValue("parent_" + i, body.get(i).getParent());

            values.append("(:author_id_").append(i).append(", ");
            values.append(":thread_id_").append(i).append(", ");
            values.append(":message_").append(i).append(", ");
            values.append(":parent_").append(i).append("), ");
        }
        values.setLength(values.length() - 2);

        template.update(
                "INSERT INTO post(author_id, thread_id, message, parent) " +
                "VALUES " + values + " RETURNING id, created", params, keys
        );

        final List<Post> results = new ArrayList<>();
        for (Integer i = 0; i < body.size(); ++i) {
            results.add(new Post(
                    ((Integer) keys.getKeyList().get(i).get("id")).longValue(),
                    authors.get(i).getNickname(),
                    authors.get(i).getId(),
                    (Timestamp) keys.getKeyList().get(i).get("created"),
                    thread.getForum(),
                    false,
                    body.get(i).getMessage(),
                    body.get(i).getParent(),
                    thread.getId()
            ));
        }
        return results;
    }


    // Проверяю наличие родителей для постов
    protected Boolean isPostsHasParents(Thread thread, List<Post> body) {
        // Создаю фейкую таблицу с body
        final StringBuilder bodyTable = new StringBuilder();
        for (Post post : body) {
            if (post.getParent() != null) {
                bodyTable.append("SELECT ").append(post.getParent()).append("AS parent UNION ");
            }
        }
        if (bodyTable.length() == 0) {
            return true;    // Добовляются только корневые посты
        }
        bodyTable.setLength(bodyTable.length() - 6);    // Убираю последний UNION

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread", thread.getId());
        try {
            // Нахожу первый BodyTable.parent которого нет в постах ветки
            template.queryForObject("SELECT BodyTable.parent "
                    + "FROM (" + bodyTable + ") AS BodyTable LEFT JOIN post P ON BodyTable.parent = P.id "
                    + "WHERE P.id IS NULL AND P.thread_id = :thread LIMIT 1", params, Long.class);
            return true;
        } catch (EmptyResultDataAccessException e) {
            return false;
        }
    }


    // Посты в ветки
    // Сообщения выводятся отсортированные по дате создания.
    public List<Post> threadPosts(String slugOrId) throws IndexOutOfBoundsException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);     // Может выпасть IndexOutOfBoundsException - ветка не найдена
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("forum", thread.getForum());
        final List<Post> posts = template.query(
                "SELECT P.id id, U.nickname author, P.author_id author_id, P.created created, :forum forum, "
                        + "P.is_edited is_edited, P.message message, P.parent parent, :thread_id thread_id "
                        + "FROM post P JOIN person U ON P.author_id = U.id "
                        + "WHERE P.thread_id = :thread_id ORDER BY created, id", params, POST_MAPPER
        );
        return posts;
    }


    // Получение полной информации о сообщении, включая связанные объекты.
    public PostFullResponse postFull(Long id) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id",id);
        return template.queryForObject(
                "SELECT P.id post_id, P.created post_created, P.is_edited post_is_edited, P.message post_message, P.parent post_parent, "   // Post
                        + "T.id thread_id, T_author.nickname thread_author, T_author.id thread_author_id, T.created thread_created, T.message thread_message, T.slug thread_slug, T.title thread_title, T.votes thread_votes, "  // Thread
                        + "F.id forum_id, F.slug forum_slug, F_user.id forum_person_id, F.title forum_title, F_user.nickname forum_user, F.posts forum_posts, F.threads forum_threads, "    // Forum
                        + "U.id user_id, U.email user_email, U.nickname user_nickname, U.fullname user_fullname, U.about user_about "   // User
                        + "FROM post P JOIN person U ON P.author_id = U.id JOIN thread T ON P.thread_id = T.id JOIN forum F ON T.forum_id = F.id "  // Post
                        + "JOIN person T_author ON T.author_id = T_author.id "  // Thread
                        + "JOIN person F_user ON F.person_id = F_user.id "      // Forum
                        + "WHERE P.id = :id", params, POST_FULL_MAPPER
        );
    }


    public static class NoParentPostException extends RuntimeException {}
}