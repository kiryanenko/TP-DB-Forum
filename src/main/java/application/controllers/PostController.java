package application.controllers;

import application.models.Post;
import application.servicies.ForumService;
import application.servicies.PostService;
import application.servicies.ThreadService;
import application.servicies.UserService;
import application.views.MessageResponse;
import application.views.PostFullResponse;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/post/{id}")
public class PostController {
    private PostService postService;
    private UserService userService;
    private ForumService forumService;
    private ThreadService threadService;


    public PostController(PostService postService,
                          UserService userService,
                          ForumService forumService,
                          ThreadService threadService) {
        this.postService = postService;
        this.userService = userService;
        this.forumService = forumService;
        this.threadService = threadService;
    }


    // Получение информации о ветке обсуждения по его имени.
    @GetMapping(path = "/details", produces = "application/json")
    public ResponseEntity details(@PathVariable Long id,
                                  @RequestParam(value="related", required=false) List<String> related) {
        final Post post;
        try {
            post = postService.findPostById(id);
        } catch (IncorrectResultSizeDataAccessException e) {
            // Пост не найден.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post with id = " + id));
        }

        final PostFullResponse postFull = new PostFullResponse(post);
        if (related != null) {
            if (related.contains("user")) {
                postFull.setAuthor(userService.findUserById(post.getAuthorId()));
            }
            if (related.contains("forum")) {
                postFull.setForum(forumService.findForumBySlug(post.getForum()));
            }
            if (related.contains("thread")) {
                postFull.setThread(threadService.findThreadById(post.getThread()));
            }
        }
        return ResponseEntity.ok(postFull);
    }


    @PostMapping(path = "/details", consumes = "application/json", produces = "application/json")
    public ResponseEntity edit(@PathVariable Long id, @RequestBody Post body) {
        try {
            return ResponseEntity.ok(postService.update(id, body));
        } catch (IncorrectResultSizeDataAccessException e) {
            // Пост не найден.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post with id = " + id));
        }
    }
}
