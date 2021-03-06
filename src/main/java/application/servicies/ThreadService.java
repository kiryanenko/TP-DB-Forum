package application.servicies;

import application.models.Forum;
import application.models.Thread;
import application.models.User;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;


@Service
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
    public Thread create(Thread body) throws IncorrectResultSizeDataAccessException, DuplicateKeyException {
        final User author = userService.findUserByNickname(body.getAuthor());   // Может выпасть IncorrectResultSizeDataAccessException - автор не найден
        final Forum forum = forumService.findForumBySlug(body.getForum());      // Может выпасть IncorrectResultSizeDataAccessException - форум не найден

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("author", author.getNickname());
        params.addValue("author_id", author.getId());
        params.addValue("forum", forum.getSlug());
        params.addValue("forum_id", forum.getId());
        params.addValue("title", body.getTitle());
        params.addValue("slug", body.getSlug());
        params.addValue("created", body.getCreated() == null ? new Date() : body.getCreated());
        params.addValue("message", body.getMessage());
        template.update("INSERT INTO thread(author, author_id, forum, forum_id, title, created, message, slug) " +
                "VALUES (:author, :author_id, :forum, :forum_id, :title, :created, :message, :slug) RETURNING id",
                params, keyHolder);
        userService.addForumUser(author.getId(), forum.getId());
        // Форум успешно создан. Возвращает данные созданного форума.
        return new Thread(keyHolder.getKey().longValue(),
                          author.getNickname(),
                          author.getId(),
                          body.getCreated(),
                          forum.getSlug(),
                          forum.getId(),
                          body.getMessage(),
                          body.getSlug(),
                          body.getTitle(),
                          null);
    }


    // Обновление ветки обсуждения на форуме.
    public Thread update(String slugOrId, Thread body) throws IncorrectResultSizeDataAccessException {
        if (body.getTitle() == null && body.getMessage() == null)
            return findThreadBySlugOrId(slugOrId);

        final MapSqlParameterSource params = new MapSqlParameterSource();
        final StringBuilder values = new StringBuilder();
        if (body.getTitle() != null) {
            params.addValue("title", body.getTitle());
            values.append("title = :title, ");
        }
        if (body.getMessage() != null) {
            params.addValue("message", body.getMessage());
            values.append("message = :message, ");
        }
        values.setLength(values.length() - 2);  // Убираю ', '

        String condition;
        try {
            params.addValue("id", Long.parseLong(slugOrId));
            condition = "id = :id";
        } catch (NumberFormatException e) {
            params.addValue("slug", slugOrId);
            condition = "LOWER(slug) = LOWER(:slug)";
        }
        return template.queryForObject(
                "UPDATE thread SET " + values + " WHERE " + condition + " RETURNING * ", params, THREAD_MAPPER
        );
    }


    public Thread findThreadById(Long id) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        return template.queryForObject("SELECT * FROM thread T WHERE T.id = :id", params, THREAD_MAPPER);
    }


    public Thread findThreadBySlug(String slug) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", slug);
        return template.queryForObject("SELECT * FROM thread T WHERE LOWER(T.slug) = LOWER(:slug)",
                params, THREAD_MAPPER);
    }


    public Thread findThreadBySlugOrId(String slugOrId) throws IncorrectResultSizeDataAccessException {
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
    public List<Thread> forumThreads(String forumSlug, Boolean isDesc, Long limit, @Nullable Date since)
            throws IncorrectResultSizeDataAccessException {
        final Forum forum = forumService.findForumBySlug(forumSlug);

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("forum_slug", forum.getSlug());
        params.addValue("forum_id", forum.getId());
        params.addValue("since", since);
        params.addValue("limit", limit);
        return template.query(
                "SELECT * FROM thread T "
                        + "WHERE T.forum_id = :forum_id "
                        + (since != null ? "AND created " + (isDesc ? "<=" : ">=") + " :since " : "")
                        + "ORDER BY created " + (isDesc ? "DESC" : "ASC")
                        + (limit != null ? " LIMIT :limit" : ""), params, THREAD_MAPPER
        );
    }
}
