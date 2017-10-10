package application.servicies;

import application.models.User;
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


    public Boolean create(User credentials) {
        final GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", credentials.getNickname());
        params.addValue("fullname", credentials.getFullname());
        params.addValue("email", credentials.getEmail());
        params.addValue("about", credentials.getAbout());
        try {
            template.update("insert into person(nickname, fullname, email, about)"
                    + " values(:nickname,:fullname,:email,:about)", params, keyHolder);
        } catch (DataAccessException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }


    // Поиск пользователя с таким же nickname или email
    public List<User> findSameUser(User credentials) {
        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("nickname", credentials.getNickname());
        params.addValue("email", credentials.getEmail());
        return template.query("select * from person where nickname=:nickname OR email=:email", params, USER_MAPPER);
    }
}
