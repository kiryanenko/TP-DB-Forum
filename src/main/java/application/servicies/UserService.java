package application.servicies;

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
public class UserService {
    private final NamedParameterJdbcTemplate template;
    @Autowired
    private ForumService forumService;


    @Autowired
    public UserService(NamedParameterJdbcTemplate template) {
        this.template = template;
    }


    private static final RowMapper<User> USER_MAPPER = (res, num) -> new User(
            res.getLong("id"),
            res.getString("nickname"),
            res.getString("fullname"),
            res.getString("email"),
            res.getString("about")
    );


    // Создание нового пользователя в базе данных.
    public User create(User credentials) throws DuplicateKeyException {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", credentials.getNickname());
        params.addValue("fullname", credentials.getFullname());
        params.addValue("email", credentials.getEmail());
        params.addValue("about", credentials.getAbout());
        template.update("INSERT INTO person (nickname, fullname, email, about)"
                        + " VALUES (:nickname,:fullname,:email,:about) RETURNING id", params, keyHolder);

        // Пользователь успешно создан. Возвращает данные созданного пользователя.
        return new User(keyHolder.getKey().longValue(),
                        credentials.getNickname(),
                        credentials.getFullname(),
                        credentials.getEmail(),
                        credentials.getAbout());
    }


    // Поиск пользователя с таким же nickname или email
    public List<User> findSameUsers(User credentials) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", credentials.getNickname());
        params.addValue("email", credentials.getEmail());
        return template.query("SELECT * FROM person WHERE LOWER(nickname) = LOWER(:nickname) OR LOWER(email) = LOWER(:email)", params, USER_MAPPER);
    }


    // Получение информации о пользователе форума по его имени.
    public User findUserByNickname(String nickname) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", nickname);
        return template.queryForObject("SELECT * FROM person WHERE LOWER(nickname) = LOWER(:nickname)", params, USER_MAPPER);
    }


    // Получение информации о пользователе форума по его id.
    public User findUserById(Long id) throws IncorrectResultSizeDataAccessException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("id", id);
        return template.queryForObject("SELECT * FROM person WHERE id = :id", params, USER_MAPPER);
    }


    // Изменение информации в профиле пользователя.
    public User update(User credentials) throws DuplicateKeyException, IncorrectResultSizeDataAccessException {
        if (credentials.getFullname() == null &&
                credentials.getEmail() == null &&
                credentials.getAbout() == null) {
            return findUserByNickname(credentials.getNickname());
        }

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", credentials.getNickname());
        final StringBuilder values = new StringBuilder();
        if (credentials.getFullname() != null) {
            params.addValue("fullname", credentials.getFullname());
            values.append("fullname = :fullname, ");
        }
        if (credentials.getEmail() != null) {
            params.addValue("email", credentials.getEmail());
            values.append("email = :email, ");
        }
        if (credentials.getAbout() != null) {
            params.addValue("about", credentials.getAbout());
            values.append("about = :about, ");
        }
        values.setLength(values.length() - 2); // Убираю последнюю ', '
        return template.queryForObject("UPDATE person SET " + values
                + " WHERE LOWER(nickname) = LOWER(:nickname) RETURNING *", params, USER_MAPPER);
    }


    // Получение списка пользователей, у которых есть пост или ветка обсуждения в данном форуме.
    // Пользователи выводятся отсортированные по nickname в порядке возрастания.
    // Порядок сотрировки должен соответсвовать побайтовому сравнение в нижнем регистре.
    public List<User> forumUsers(String forumSlug, Long limit, String since, Boolean isDesc)
            throws IncorrectResultSizeDataAccessException {
        final Long forumId = forumService.getForumIdWithSlug(forumSlug);
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("forum_id", forumId);
        params.addValue("since", since);
        params.addValue("limit", limit);
        return template.query(
                "SELECT U.* FROM person U JOIN forum_person FP ON U.id = FP.person_id "
                        + "WHERE FP.forum_id = :forum_id "
                        + (since != null ? "AND LOWER(U.nickname) " + (isDesc ? "<" : ">") + " LOWER(:since)" : "")
                        + " ORDER BY LOWER(U.nickname) " + (isDesc ? "DESC" : "ASC") + ' '
                        + (limit != null ? "LIMIT :limit" : ""), params, USER_MAPPER);
    }


    public void addForumUser(Long userId, Long forumId) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("forum_id", forumId);
        params.addValue("person_id", userId);
        template.update("INSERT INTO forum_person(person_id, forum_id) VALUES (:person_id, :forum_id) "
                + "ON CONFLICT (person_id, forum_id) DO NOTHING", params);
    }
}
