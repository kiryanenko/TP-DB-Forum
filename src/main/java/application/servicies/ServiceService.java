package application.servicies;

import application.models.ServiceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class ServiceService {
    private final NamedParameterJdbcTemplate template;


    @Autowired
    public ServiceService(NamedParameterJdbcTemplate template) {
        this.template = template;
    }


    private static final RowMapper<ServiceStatus> STATUS_MAPPER = (res, num) -> new ServiceStatus(
            res.getLong("n_forum"),
            res.getLong("n_post"),
            res.getLong("n_thread"),
            res.getLong("n_user")
    );


    // Безвозвратное удаление всей пользовательской информации из базы данных.
    public void clear() {
        template.update("TRUNCATE person, forum, post, thread, vote", new MapSqlParameterSource());
    }


    // Кол-во записей в базе данных, включая помеченные как "удалённые".
    public ServiceStatus status() {
        return template.queryForObject("SELECT (SELECT count(*) FROM person) n_user, "
                + "(SELECT count(*) FROM forum) n_forum, "
                + "(SELECT count(*) FROM post) n_post, "
                + "(SELECT count(*) FROM thread) n_thread ", new MapSqlParameterSource(), STATUS_MAPPER);
    }
}
