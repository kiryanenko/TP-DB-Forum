package application.servicies;

import application.models.User;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@Transactional
public class UserService {
    private final NamedParameterJdbcTemplate template;


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
        template.update("INSERT INTO person(nickname, fullname, email, about)"
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
        return template.query("SELECT * FROM person WHERE nickname=:nickname OR email=:email", params, USER_MAPPER);
    }


    // Получение информации о пользователе форума по его имени.
    public User findUserByNickname(String nickname) throws IndexOutOfBoundsException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", nickname);
        final List<User> res = template.query("SELECT * FROM person WHERE nickname=:nickname LIMIT 1", params, USER_MAPPER);
        return res.get(0);  // Может выпасть IndexOutOfBoundsException - пользователь не найден
    }


    // Изменение информации в профиле пользователя.
    public User update(User credentials) throws DataAccessException, IndexOutOfBoundsException {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", credentials.getNickname());
        params.addValue("fullname", credentials.getFullname());
        params.addValue("email", credentials.getEmail());
        params.addValue("about", credentials.getAbout());
        final List<User> res = template.query("UPDATE person SET fullname=:fullname, email=:email, about=:about"
                + " WHERE nickname=:nickname RETURNING *", params, USER_MAPPER);

        // Пользователь успешно создан. Возвращает данные созданного пользователя.
        return res.get(0);  // Может выпасть IndexOutOfBoundsException - пользователь не найден
    }
}
