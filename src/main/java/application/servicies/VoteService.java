package application.servicies;

import application.models.Thread;
import application.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;


@Service
public class VoteService {
    private final NamedParameterJdbcTemplate template;
    private ThreadService threadService;


    @Autowired
    public VoteService(NamedParameterJdbcTemplate template, ThreadService threadService) {
        this.template = template;
        this.threadService = threadService;
    }


    private static final RowMapper<Vote> VOTE_MAPPER = (res, num) -> new Vote(
            res.getLong("id"),
            res.getLong("person_id"),
            res.getLong("thread_id"),
            res.getInt("voice")
    );


    // Изменение голоса за ветвь обсуждения.
    // Один пользователь учитывается только один раз и может изменить своё мнение.
    public Thread vote(String slugOrId, Vote vote) throws IncorrectResultSizeDataAccessException, DataIntegrityViolationException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("nickname", vote.getNickname());
        params.addValue("voice", vote.getVoice());

        try {
            final Vote existedVote = template.queryForObject("SELECT * FROM vote "
                            + "WHERE thread_id = :thread_id AND person_id = (SELECT id FROM person WHERE nickname = :nickname) ",
                    params, VOTE_MAPPER
            );  // Вылетает DataIntegrityViolationException, когда не найден пользователь

            if (Objects.equals(vote.getVoice(), existedVote.getVoice())) {
                return thread;
            }

            params.addValue("id", existedVote.getId());
            template.update("UPDATE vote SET voice = :voice WHERE id = :id", params);

            thread.setVotes(thread.getVotes() - existedVote.getVoice() + vote.getVoice());

        } catch (IncorrectResultSizeDataAccessException e) { // Пользователь не голосовал
            template.update("INSERT INTO vote(thread_id, person_id, voice) "
                    + "VALUES (:thread_id, (SELECT id FROM person WHERE nickname = :nickname), :voice) ", params
            );  // Вылетает DataIntegrityViolationException, когда не найден пользователь

            thread.setVotes(thread.getVotes() + vote.getVoice());
        }

        return thread;
    }
}
