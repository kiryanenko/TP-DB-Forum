package application.servicies;

import application.models.Thread;
import application.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class VoteService {
    private final NamedParameterJdbcTemplate template;
    private ThreadService threadService;


    @Autowired
    public VoteService(NamedParameterJdbcTemplate template, UserService userService, ThreadService threadService) {
        this.template = template;
        this.threadService = threadService;
    }


    // Изменение голоса за ветвь обсуждения.
    // Один пользователь учитывается только один раз и может изменить своё мнение.
    public Thread vote(String slugOrId, Vote vote) throws IncorrectResultSizeDataAccessException, DataIntegrityViolationException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("nickname", vote.getNickname());
        params.addValue("voice", vote.getVoice());
        template.update("INSERT INTO vote(thread_id, person_id, voice) "
                + "VALUES (:thread_id, (SELECT id FROM person WHERE nickname = :nickname), :voice) "
                + "ON CONFLICT (thread_id, person_id) DO UPDATE SET voice = :voice", params
        );  // Вылетает DataIntegrityViolationException, когда не найден пользователь

        return threadService.findThreadById(thread.getId());
    }
}
