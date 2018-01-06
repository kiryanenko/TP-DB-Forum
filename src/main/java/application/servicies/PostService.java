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
public class PostService {
    private final NamedParameterJdbcTemplate template;
    private final UserService userService;
    private final ThreadService threadService;
    private final ForumService forumService;


    @Autowired
    public PostService(NamedParameterJdbcTemplate template,
                       UserService userService,
                       ThreadService threadService,
                       ForumService forumService) {
        this.template = template;
        this.userService = userService;
        this.threadService = threadService;
        this.forumService = forumService;
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
    public List<Post> createPosts(String slugOrId, List<Post> body) throws IncorrectResultSizeDataAccessException, NoParentPostException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);     // Может выпасть IncorrectResultSizeDataAccessException - ветка не найдена

        if (body.isEmpty()) {
            return new ArrayList<>();
        }

        if (!isPostsHasParents(thread, body)) {
            throw new NoParentPostException();
        }

        final MapSqlParameterSource params = new MapSqlParameterSource();
        final StringBuilder values = new StringBuilder();

        for (Integer i = 0; i < body.size(); ++i) {
            final User author = userService.findUserByNickname(body.get(i).getAuthor());   // Может выпасть IndexOutOfBoundsException - автор не найден
            userService.addForumUser(author.getId(), thread.getForumId());

            final Integer id = template.queryForObject("SELECT nextval('posts_id_seq')",
                    new MapSqlParameterSource(), Integer.class);

            params.addValue("id_" + i, id);
            params.addValue("author_id_" + i, author.getId());
            params.addValue("author_" + i, author.getNickname());
            params.addValue("thread_id_" + i, thread.getId());
            params.addValue("forum_id_" + i, thread.getForumId());
            params.addValue("forum_" + i, thread.getForum());
            params.addValue("message_" + i, body.get(i).getMessage());
            params.addValue("parent_" + i, body.get(i).getParent());

            values.append("(:id_").append(i).append(", ");
            values.append(":author_id_").append(i).append(", ");
            values.append(":author_").append(i).append(", ");
            values.append(":thread_id_").append(i).append(", ");
            values.append(":forum_id_").append(i).append(", ");
            values.append(":forum_").append(i).append(", ");
            values.append(":message_").append(i).append(", ");
            values.append(":parent_").append(i).append(", ");
            values.append("((SELECT path FROM post WHERE id = :parent_").append(i).append(") || :id_").append(i).append(")[1], ");
            values.append("(SELECT path FROM post WHERE id = :parent_").append(i).append(") || :id_").append(i).append("), ");
        }
        values.setLength(values.length() - 2);

        final List<Post> results = template.query(
                "INSERT INTO post(id, author_id, author, thread_id, forum_id, forum, message, parent, root, path) " +
                "VALUES " + values + " RETURNING *", params, POST_MAPPER
        );
        forumService.incForumPostsIncludedThread(thread.getId(), body.size());
        return results;
    }


    // Проверяю наличие родителей для постов
    protected Boolean isPostsHasParents(Thread thread, List<Post> body) {
        // Создаю фейковую таблицу с body
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
                    + "WHERE P.id IS NULL OR P.thread_id != :thread LIMIT 1", params, Long.class);
            return false;
        } catch (EmptyResultDataAccessException e) {
            return true;
        }
    }


    // Посты в ветки
    // Сообщения выводятся отсортированные по дате создания.
    public List<Post> threadPostsFlat(String slugOrId, Long limit, Long since, Boolean isDesc)
            throws IncorrectResultSizeDataAccessException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);     // Может выпасть IncorrectResultSizeDataAccessException - ветка не найдена
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("forum", thread.getForum());
        params.addValue("since", since);
        params.addValue("limit", limit);
        return template.query(
                "SELECT * FROM post P "
                        + "WHERE P.thread_id = :thread_id "
                        + (since != null ? "AND P.id " + (isDesc ? '<' : '>') + " :since " : "")
                        + "ORDER BY id " + (isDesc ? "DESC" : "ASC")
                        + (limit != null ? " LIMIT :limit" : ""),
                params, POST_MAPPER
        );
    }


    // Посты в ветки
    // Сообщения выводятся отсортированные по дате создания.
    // Древовидный, комментарии выводятся отсортированные в дереве по N штук
    public List<Post> threadPostsTree(String slugOrId, Long limit, Long since, Boolean isDesc)
            throws IncorrectResultSizeDataAccessException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);     // Может выпасть IncorrectResultSizeDataAccessException - ветка не найдена
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("forum", thread.getForum());
        params.addValue("since", since);
        params.addValue("limit", limit);
        return template.query(
                "SELECT * FROM post P "
                        + "WHERE P.thread_id = :thread_id "
                        + (since != null ? "AND P.path " + (isDesc ? '<' : '>') + " (SELECT path FROM post WHERE id = :since) " : "")
                        + "ORDER BY P.path " + (isDesc ? "DESC" : "ASC")
                        + (limit != null ? " LIMIT :limit" : ""),
                params, POST_MAPPER
        );
    }


    // Посты в ветки
    // Сообщения выводятся отсортированные по дате создания.
    // Древовидные с пагинацией по родительским (parent_tree), на странице N родительских комментов
    // и все комментарии прикрепленные к ним, в древвидном отображение.
    public List<Post> threadPostsParentTree(String slugOrId, Long limit, Long since, Boolean isDesc)
            throws IncorrectResultSizeDataAccessException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);     // Может выпасть IncorrectResultSizeDataAccessException - ветка не найдена
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("forum", thread.getForum());
        params.addValue("since", since);
        params.addValue("limit", limit);
        return template.query(
                "WITH roots AS ("
                        + " SELECT id FROM post "
                        + " WHERE thread_id = :thread_id AND parent IS NULL "
                        + (since != null ? "AND path " + (isDesc ? '<' : '>') + " (SELECT path FROM post WHERE id = :since)" : "")
                        + " ORDER BY id " + (isDesc ? "DESC" : "ASC")
                        + (limit != null ? " LIMIT :limit" : "")
                        + ") "
                        + "SELECT P.* FROM post P JOIN roots ON roots.id = P.root "
                        + "ORDER BY P.path " + (isDesc ? "DESC" : "ASC"),
                params, POST_MAPPER
        );
    }


    // Получение полной информации о сообщении, включая связанные объекты.
    public PostFullResponse postFull(Long id) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
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


    // Получение полной информации о сообщении, включая связанные объекты.
    public Post findPostById(Long id) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        return template.queryForObject("SELECT * FROM post P WHERE P.id = :id", params, POST_MAPPER);
    }


    // Изменение сообщения на форуме.
    // Если сообщение поменяло текст, то оно должно получить отметку isEdited.
    public Post update(Long id, Post body) throws IncorrectResultSizeDataAccessException {
        if (body.getMessage() == null) {
            return findPostById(id);
        }

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        params.addValue("message", body.getMessage());
        return template.queryForObject(
                "UPDATE post SET message = :message, is_edited = is_edited OR message != :message "
                        + "WHERE id = :id RETURNING *, "
                        + "(SELECT P.nickname FROM person P WHERE P.id = author_id) author, "
                        + "(SELECT F.slug FROM thread T JOIN forum F ON T.forum_id = F.id WHERE T.id = thread_id) forum",
                params, POST_MAPPER
        );
    }


    public static class NoParentPostException extends RuntimeException {}
}
