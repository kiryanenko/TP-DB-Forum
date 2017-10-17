package application.servicies;

import application.models.Post;
import application.models.Thread;
import application.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


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


    public static class NoParentPostException extends RuntimeException {}
}
