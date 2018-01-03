package application.servicies;

import application.models.Thread;
import application.models.User;
import application.models.Vote;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Objects;


@Service
public class VoteService {
    private final NamedParameterJdbcTemplate template;
    private ThreadService threadService;
    private UserService userService;

    @Autowired
    public VoteService(NamedParameterJdbcTemplate template, ThreadService threadService, UserService userService) {
        this.template = template;
        this.threadService = threadService;
        this.userService = userService;
    }


    private static final RowMapper<Vote> VOTE_MAPPER = (res, num) -> new Vote(
            res.getLong("id"),
            res.getLong("person_id"),
            res.getLong("thread_id"),
            res.getInt("voice")
    );


    // Изменение голоса за ветвь обсуждения.
    // Один пользователь учитывается только один раз и может изменить своё мнение.
    public Thread vote(String slugOrId, Vote vote) throws IncorrectResultSizeDataAccessException {
        final Thread thread = threadService.findThreadBySlugOrId(slugOrId);
        final User user = userService.findUserByNickname(vote.getNickname());

        final MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("thread_id", thread.getId());
        params.addValue("person_id", user.getId());
        params.addValue("voice", vote.getVoice());

        try {
            final Vote existedVote = template.queryForObject("SELECT * FROM vote "
                            + "WHERE thread_id = :thread_id AND person_id = :person_id ",
                    params, VOTE_MAPPER
            );

            if (Objects.equals(vote.getVoice(), existedVote.getVoice())) {
                return thread;
            }

            params.addValue("id", existedVote.getId());
            template.update("UPDATE vote SET voice = :voice WHERE id = :id", params);

            thread.setVotes(thread.getVotes() - existedVote.getVoice() + vote.getVoice());

        } catch (IncorrectResultSizeDataAccessException e) { // Пользователь не голосовал
            template.update("INSERT INTO vote(thread_id, person_id, voice) "
                    + "VALUES (:thread_id, :person_id, :voice) ", params
            );

            thread.setVotes(thread.getVotes() + vote.getVoice());
        }

        return thread;
    }
}
