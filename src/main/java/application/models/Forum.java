package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

public class Forum {
    private Long id;

    // Общее кол-во сообщений в данном форуме.
    // readOnly: true
    // example: 200000
    @JsonProperty("posts")
    private Long posts;

    // Человекопонятный URL
    // (https://ru.wikipedia.org/wiki/%D0%A1%D0%B5%D0%BC%D0%B0%D0%BD%D1%82%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%B8%D0%B9_URL),
    // уникальное поле.
    // pattern: ^(\d|\w|-|_)*(\w|-|_)(\d|\w|-|_)*$
    // x-isnullable: false
    // example: pirate-stories
    @JsonProperty("slug")
    private String slug;

    // Общее кол-во ветвей обсуждения в данном форуме.
    // readOnly: true
    // example: 200
    @JsonProperty("threads")
    private Long threads;

    // Название форума.
    // x-isnullable: false
    // example: Pirate stories
    @JsonProperty("title")
    private String title;

    // Nickname пользователя, который отвечает за форум.
    // x-isnullable: false
    // example: j.sparrow
    @JsonProperty("user")
    private String userNickname;

    // Id пользователя, который отвечает за форум.
    private Long userId;


    @JsonCreator
    public Forum(@JsonProperty("id") Long id,
                 @JsonProperty("slug") String slug,
                 @JsonProperty("title") String title,
                 @JsonProperty("user") String userNickname,
                 @JsonProperty("user_id") Long userId,
                 @Nullable @JsonProperty("posts") Long posts,
                 @Nullable @JsonProperty("threads") Long threads) {
        this.id = id;
        this.slug = slug;
        this.title = title;
        this.userNickname = userNickname;
        this.userId = userId;
        this.posts = posts == null ? 0 : posts;
        this.threads = threads == null ? 0 : threads;
    }


    @JsonIgnore
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPosts() {
        return posts;
    }

    public void setPosts(Long posts) {
        this.posts = posts;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public Long getThreads() {
        return threads;
    }

    public void setThreads(Long threads) {
        this.threads = threads;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    @JsonIgnore
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
