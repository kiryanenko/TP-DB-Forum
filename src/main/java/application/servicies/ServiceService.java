package application.servicies;

import org.springframework.beans.factory.annotation.Autowired;
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


    // Безвозвратное удаление всей пользовательской информации из базы данных.
    public void clear() {
        template.update("TRUNCATE person, forum, post, thread, vote", new MapSqlParameterSource());
    }
}
