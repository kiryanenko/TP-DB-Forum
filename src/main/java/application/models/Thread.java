package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;


// Ветка обсуждения на форуме
public class Thread {
    // Идентификатор ветки обсуждения
    @JsonProperty("id")
    private Long id;

    // Пользователь, создавший данную тему
    // x-isnullable: false
    // example: j.sparrow
    @JsonProperty("author")
    private String author;

    private Long authorId;

    // Дата создания ветки на форуме
    // x-isnullable: true
    // example: 2017-01-01T00:00:00.000Z
    @JsonProperty("created")
    private Timestamp created;

    // Форум, в котором расположена данная ветка обсуждени
    // readOnly: true
    // example: pirate-stories
    @JsonProperty("forum")
    private String forum;

    private Long forumId;

    // Описание ветки обсуждения
    // x-isnullable: false
    // example: An urgent need to reveal the hiding place of Davy Jones. Who is willing to help in this matter?
    @JsonProperty("message")
    private String message;

    // Человекопонятный URL
    // (https://ru.wikipedia.org/wiki/%D0%A1%D0%B5%D0%BC%D0%B0%D0%BD%D1%82%D0%B8%D1%87%D0%B5%D1%81%D0%BA%D0%B8%D0%B9_URL).
    // В данной структуре slug опционален и не может быть числом.
    // pattern: ^(\d|\w|-|_)*(\w|-|_)(\d|\w|-|_)*$
    // readOnly: true
    // example: jones-cache
    @JsonProperty("slug")
    private String slug;

    // Заголовок ветки обсуждения
    // x-isnullable: false
    // example: Davy Jones cache
    @JsonProperty("title")
    private String title;

    // Кол-во голосов непосредственно за данное сообщение форума
    // readOnly: true
    @JsonProperty("votes")
    private Long votes;


    @JsonCreator
    public Thread(@JsonProperty("id") Long id,
                  @JsonProperty("author") String author,
                  @JsonProperty("authorId") Long authorId,
                  @JsonProperty("created") Timestamp created,
                  @JsonProperty("forum") String forum,
                  @JsonProperty("forumId") Long forumId,
                  @JsonProperty("message") String message,
                  @Nullable @JsonProperty("slug") String slug,
                  @JsonProperty("title") String title,
                  @Nullable @JsonProperty("votes") Long votes) {
        this.id = id;
        this.author = author;
        this.authorId = authorId;
        this.created = created;
        this.forum = forum;
        this.forumId = forumId;
        this.message = message;
        this.slug = slug;
        this.title = title;
        this.votes = votes == null ? 0 : votes;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    @JsonIgnore
    public Timestamp getCreated() {
        return created;
    }

    public void setCreated(Timestamp created) {
        this.created = created;
    }

    public String getForum() {
        return forum;
    }

    public void setForum(String forum) {
        this.forum = forum;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getVotes() {
        return votes;
    }

    public void setVotes(Long votes) {
        this.votes = votes;
    }

    @JsonIgnore
    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    @JsonIgnore
    public Long getForumId() {
        return forumId;
    }

    public void setForumId(Long forumId) {
        this.forumId = forumId;
    }


    @JsonProperty("created")
    public String getCreatedAsString() {
        return created.toString();
    }
}
