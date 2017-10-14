package application.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.jetbrains.annotations.Nullable;

import java.sql.Timestamp;

public class Post {
    // Идентификатор
    @JsonProperty("id")
    private Long id;

    // Автор, написавший данное сообщение.
    // x-isnullable: false
    // example: j.sparrow
    @JsonProperty("author")
    private String author;

    @JsonProperty("author_id")
    private Long authorId;

    // Дата создания сообщения на форуме
    // x-isnullable: true
    // example: 2017-01-01T00:00:00.000Z
    private Timestamp created;

    // Форум, в котором расположен
    // readOnly: true
    // example: pirate-stories
    @JsonProperty("forum")
    private String forum;

    // Истина, если данное сообщение было изменено.
    // x-isnullable: false
    // readOnly: true
    @JsonProperty("isEdited")
    private Boolean isEdited;

    // Собственно сообщение форума.
    // x-isnullable: false
    // example: We should be afraid of the Kraken.
    @JsonProperty("message")
    private String message;

    // Идентификатор родительского сообщения (0 - корневое сообщение обсуждения).
    private Long parent;

    // Идентификатор ветви (id) обсуждения данного сообещния.
    @JsonProperty("thread")
    private Long thread;


    @JsonCreator
    public Post(@Nullable @JsonProperty("id") Long id,
                @JsonProperty("author") String author,
                @JsonProperty("author_id") Long authorId,
                @JsonProperty("created") Timestamp created,
                @JsonProperty("forum") String forum,
                @JsonProperty("isEdited") Boolean isEdited,
                @JsonProperty("message") String message,
                @JsonProperty("parent") Long parent,
                @JsonProperty("thread") Long thread) {
        this.id = id;
        this.author = author;
        this.authorId = authorId;
        this.created = created;
        this.forum = forum;
        this.isEdited = isEdited;
        this.message = message;
        this.parent = parent == null || parent == 0 ? null : parent;
        this.thread = thread;
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

    public Boolean getEdited() {
        return isEdited;
    }

    public void setEdited(Boolean edited) {
        isEdited = edited;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @JsonIgnore
    public Long getParent() {
        return parent;
    }

    public void setParent(Long parent) {
        this.parent = parent == null || parent == 0 ? null : parent;
    }

    public Long getThread() {
        return thread;
    }

    public void setThread(Long thread) {
        this.thread = thread;
    }

    @JsonIgnore
    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }


    @JsonProperty("created")
    public String getCreatedAsString() {
        return created.toString();
    }


    @JsonProperty("parent")
    public Long getParentForJSON() {
        return parent == null ? 0 : parent;
    }
}
