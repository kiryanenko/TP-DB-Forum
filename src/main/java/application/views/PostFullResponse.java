package application.views;

import application.models.Forum;
import application.models.Post;
import application.models.User;
import application.models.Thread;
import com.fasterxml.jackson.annotation.JsonProperty;


// Полная информация о сообщении, включая связанные объекты.
public class PostFullResponse {
    @JsonProperty("author")
    private User author;

    @JsonProperty("forum")
    private Forum forum;

    @JsonProperty("post")
    private Post post;

    @JsonProperty("thread")
    private Thread thread;


    public PostFullResponse(User author, Forum forum, Post post, Thread thread) {
        this.author = author;
        this.forum = forum;
        this.post = post;
        this.thread = thread;
    }


    public User getAuthor() {
        return author;
    }

    public Forum getForum() {
        return forum;
    }

    public Post getPost() {
        return post;
    }

    public Thread getThread() {
        return thread;
    }
}
