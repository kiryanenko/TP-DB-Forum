package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


// Информация о голосовании пользователя.
public class Vote {
    // Идентификатор голоса.
    private Long id;
    // Идентификатор ветки обсуждения.
    private Long threadId;
    // Идентификатор ветки пользователя.
    private Long userId;

    // Идентификатор пользователя.
    @JsonProperty("nickname")
    private String nickname;

    // Отданный голос.
    // Enum: [ -1, 1 ]
    @JsonProperty("voice")
    private Integer voice;


    @JsonCreator
    public Vote(@JsonProperty("threadId") Long threadId,
                @JsonProperty("userId") Long userId,
                @JsonProperty("nickname") String nickname,
                @JsonProperty("voice") Integer voice) {
        this.threadId = threadId;
        this.userId = userId;
        this.nickname = nickname;
        this.voice = voice;
    }


    public Vote(Long id, Long threadId, Long userId, Integer voice) {
        this.id = id;
        this.threadId = threadId;
        this.userId = userId;
        this.voice = voice;
    }

    public Long getThreadId() {
        return threadId;
    }

    public void setThreadId(Long threadId) {
        this.threadId = threadId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public Integer getVoice() {
        return voice;
    }

    public void setVoice(Integer voice) {
        this.voice = voice;
    }

    public Long getId() {
        return id;
    }
}
