package application.views;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;


public class ServiceStatusResponse {
    // Кол-во разделов в базе данных.
    @JsonProperty("forum")
    private Long forum;

    // Кол-во сообщений в базе данных.
    @JsonProperty("post")
    private Long post;

    // Кол-во веток обсуждения в базе данных.
    @JsonProperty("thread")
    private Long thread;

    // Кол-во пользователей в базе данных.
    @JsonProperty("user")
    private Long user;


    @JsonCreator
    public ServiceStatusResponse(@JsonProperty("forum") Long forum,
                                 @JsonProperty("post") Long post,
                                 @JsonProperty("thread") Long thread,
                                 @JsonProperty("user") Long user) {
        this.forum = forum;
        this.post = post;
        this.thread = thread;
        this.user = user;
    }


    public Long getForum() {
        return forum;
    }

    public Long getPost() {
        return post;
    }

    public Long getThread() {
        return thread;
    }

    public Long getUser() {
        return user;
    }
}
