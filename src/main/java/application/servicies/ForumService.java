package application.servicies;

import application.models.Forum;
import application.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
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
    private UserService userService;


    @Autowired
    public ForumService(NamedParameterJdbcTemplate template, UserService userService) {
        this.template = template;
        this.userService = userService;
    }


    private static final RowMapper<Forum> FORUM_MAPPER = (res, num) -> new Forum(
            res.getLong("id"),
            res.getString("slug"),
            res.getString("title"),
            res.getString("nickname"),
            res.getLong("person_id"),
            res.getLong("posts"),
            res.getLong("threads")
    );


    // Создание нового форума.
    public Forum create(Forum body) throws IndexOutOfBoundsException, DataAccessException {
        final User user = userService.findUserByNickname(body.getUserNickname());   // Может выпасть IndexOutOfBoundsException - пользователь не найден

        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", body.getSlug());
        params.addValue("user_id", user.getId());
        params.addValue("title", body.getTitle());
        template.update("INSERT INTO forum(slug, person_id, title) VALUES (:slug, :user_id, :title) RETURNING id", params, keyHolder);
        // Форум успешно создан. Возвращает данные созданного форума.
        return new Forum(keyHolder.getKey().longValue(),
                         body.getSlug(),
                         body.getTitle(),
                         body.getUserNickname(),
                         body.getUserId(),
                         null, null);
    }


    // Получение информации о форуме по его идентификаторе.
    public Forum findForumBySlug(String slug) throws IndexOutOfBoundsException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("slug", slug);
        final List<Forum> res = template.query("SELECT F.id id, slug, person_id, title, nickname, posts, threads "
                + "FROM forum F INNER JOIN person P ON P.id = F.person_id WHERE F.slug=:slug LIMIT 1", params, FORUM_MAPPER);
        return res.get(0);  // Может выпасть IndexOutOfBoundsException - форум не найден
    }
}
