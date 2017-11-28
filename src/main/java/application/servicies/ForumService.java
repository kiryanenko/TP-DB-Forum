package application.servicies;

import application.models.Forum;
import application.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
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
public class ForumService {
    private final NamedParameterJdbcTemplate template;
    @Autowired
    private UserService userService;


    @Autowired
    public ForumService(NamedParameterJdbcTemplate template) {
        this.template = template;
    }


    private static final RowMapper<Forum> FORUM_MAPPER = (res, num) -> new Forum(
            res.getLong("id"),
            res.getString("slug"),
            res.getString("title"),
            res.getString("person_nickname"),
            res.getLong("person_id"),
            res.getLong("posts"),
            res.getLong("threads")
    );


    // Создание нового форума.
    public Forum create(Forum body) throws DataIntegrityViolationException, DuplicateKeyException {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", body.getSlug());
        params.addValue("title", body.getTitle());
        params.addValue("user", body.getUserNickname());
        return template.queryForObject("WITH get_user AS (SELECT id, nickname FROM person WHERE LOWER(nickname) = LOWER(:user)) "
                + "INSERT INTO forum(slug, person_id, title) "
                + "VALUES (:slug, (SELECT id FROM get_user), :title) RETURNING *, (SELECT nickname FROM get_user) person_nickname",
                params, FORUM_MAPPER);
    }


    // Получение информации о форуме по его идентификаторе.
    public Forum findForumBySlug(String slug) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", slug);
        return template.queryForObject(
                "SELECT F.id id, F.slug slug, F.person_id person_id, F.title title, P.nickname person_nickname, F.posts posts, F.threads threads "
                        + "FROM forum F JOIN person P ON P.id = person_id WHERE LOWER(F.slug) = LOWER(:slug)",
                params, FORUM_MAPPER);
    }


    // Получение id форумa по его идентификатору
    public Long getForumIdWithSlug(String slug) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", slug);
        return template.queryForObject("SELECT id FROM forum WHERE LOWER(slug) = LOWER(:slug)", params, Long.class);
    }


    public void incForumPostsIncludedThread(Long threadId, Integer count) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", threadId);
        params.addValue("count", count);
        template.update("UPDATE forum SET posts = posts + :count "
                + "WHERE id = (SELECT forum_id FROM thread WHERE id = :thread_id)", params);
    }
}
