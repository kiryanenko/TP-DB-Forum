package application.servicies;

import application.models.Forum;
import application.models.Thread;
import application.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class ThreadService {
    private final NamedParameterJdbcTemplate template;
    private final UserService userService;
    private final ForumService forumService;


    @Autowired
    public ThreadService(NamedParameterJdbcTemplate template, UserService userService, ForumService forumService) {
        this.template = template;
        this.userService = userService;
        this.forumService = forumService;
    }


    private static final RowMapper<Thread> THREAD_MAPPER = (res, num) -> new Thread(
            res.getLong("id"),
            res.getString("author"),
            res.getLong("author_id"),
            res.getTimestamp("created"),
            res.getString("forum"),
            res.getLong("forum_id"),
            res.getString("message"),
            res.getString("slug"),
            res.getString("title"),
            res.getLong("votes")
    );


    // Добавление новой ветки обсуждения на форум
    public Thread create(Thread body) throws IndexOutOfBoundsException, DuplicateKeyException {
        final User author = userService.findUserByNickname(body.getAuthor());   // Может выпасть IndexOutOfBoundsException - автор не найден
        final Forum forum = forumService.findForumBySlug(body.getForum());      // Может выпасть IndexOutOfBoundsException - форум не найден

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("author_id", author.getId());
        params.addValue("forum_id", forum.getId());
        params.addValue("title", body.getTitle());
        params.addValue("slug", body.getSlug());
        params.addValue("message", body.getMessage());
        template.update("INSERT INTO thread(author_id, forum_id, title, created, message, slug) " +
                "VALUES (:author_id, :forum_id, :title, now(), :message, :slug) RETURNING id", params, keyHolder);
        // Форум успешно создан. Возвращает данные созданного форума.
        return new Thread(keyHolder.getKey().longValue(),
                          body.getAuthor(),
                          author.getId(),
                          body.getCreated(),
                          body.getForum(),
                          forum.getId(),
                          body.getMessage(),
                          body.getSlug(),
                          body.getTitle(),
                          null);
    }


    // Обновление ветки обсуждения на форуме.
    public Thread update(String slugOrId, Thread body) throws IndexOutOfBoundsException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug_or_id", slugOrId);
        params.addValue("title", body.getTitle());
        params.addValue("message", body.getMessage());

        String condition;
        try {
            params.addValue("id", Long.parseLong(slugOrId));
            condition = "T.id = :id";
        } catch (NumberFormatException e) {
            params.addValue("slug", slugOrId);
            condition = "T.slug = :slug";
        }
        final List<Thread> res = template.query(
                "UPDATE thread SET title = :title, message = :message " +
                        "FROM thread T JOIN person P ON P.id = author_id JOIN forum F ON F.id = forum_id " +
                        "WHERE "+ condition + " RETURNING *, nickname author, F.slug forum", params, THREAD_MAPPER
        );
        return res.get(0);
    }


    public Thread findThreadById(Long id) throws IndexOutOfBoundsException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        final List<Thread> res = template.query(
                "SELECT T.id id, P.nickname author, author_id, created, F.slug forum, forum_id, message, T.slug slug, T.title title, votes " +
                        "FROM thread T JOIN person P ON P.id = author_id JOIN forum F ON F.id = forum_id " +
                        "WHERE T.id=:id LIMIT 1", params, THREAD_MAPPER
        );
        return res.get(0);  // Может выпасть IndexOutOfBoundsException - ветвь не найдена
    }


    public Thread findThreadBySlug(String slug) throws IndexOutOfBoundsException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", slug);
        final List<Thread> res = template.query(
                "SELECT T.id id, P.nickname author, author_id, created, F.slug forum, forum_id, message, T.slug slug, T.title title, votes " +
                        "FROM thread T JOIN person P ON P.id = author_id JOIN forum F ON F.id = forum_id " +
                        "WHERE T.slug=:slug LIMIT 1", params, THREAD_MAPPER
        );
        return res.get(0);  // Может выпасть IndexOutOfBoundsException - ветвь не найдена
    }


    public Thread findThreadBySlugOrId(String slugOrId) throws IndexOutOfBoundsException {
        try {
            final Long id = Long.parseLong(slugOrId);
            return findThreadById(id);
        } catch (NumberFormatException e) {
            return findThreadBySlug(slugOrId);
        }
    }


    public Long getThreadIdWithSlug(String slug) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", slug);
        return template.queryForObject("SELECT id FROM thread WHERE slug = :slug", params, Long.class);
    }


    // Получение списка ветвей обсужления данного форума.
    // Ветви обсуждения выводятся отсортированные по дате создания.
    public List<Thread> forumThreads(String forumSlug) throws IndexOutOfBoundsException {
        forumService.findForumBySlug(forumSlug); // Может выпасть IndexOutOfBoundsException - форум не найден

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", forumSlug);
        return template.query(
                "SELECT T.id id, P.nickname author, author_id, created, F.slug forum, forum_id, message, T.slug slug, T.title title, votes " +
                        "FROM thread T JOIN person P ON P.id = author_id JOIN forum F ON F.id = forum_id " +
                        "WHERE F.slug=:slug ORDER BY created", params, THREAD_MAPPER
        );
    }
}
