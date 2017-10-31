package application.controllers;

import application.models.Post;
import application.models.Thread;
import application.models.Vote;
import application.servicies.PostService;
import application.servicies.ThreadService;
import application.servicies.VoteService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/thread/{slug_or_id}")
public class ThreadController {
    private ThreadService threadService;
    private PostService postService;
    private VoteService voteService;


    @Autowired
    public ThreadController(ThreadService threadService, PostService postService, VoteService voteService) {
        this.threadService = threadService;
        this.postService = postService;
        this.voteService = voteService;
    }


    // Получение информации о ветке обсуждения по его имени.
    @GetMapping(path = "/details", produces = "application/json")
    public ResponseEntity details(@PathVariable("slug_or_id") String slugOrId) {
        try {
            return ResponseEntity.ok(threadService.findThreadBySlugOrId(slugOrId));
        } catch (IndexOutOfBoundsException e) {
            // Ветка обсуждения отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread " + slugOrId));
        }
    }


    // Обновление ветки обсуждения на форуме.
    @PostMapping(path = "/details", consumes = "application/json", produces = "application/json")
    public ResponseEntity update(@RequestBody Thread body, @PathVariable("slug_or_id") String slugOrId) {
        try {
            return ResponseEntity.ok(threadService.update(slugOrId, body));
        } catch (IndexOutOfBoundsException e) {
            // Ветка обсуждения отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread " + slugOrId));
        }
    }


    // Добавление новых постов в ветку обсуждения на форум.
    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity createPosts(@RequestBody List<Post> body, @PathVariable("slug_or_id") String slugOrId) {
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(postService.createPosts(slugOrId, body));
        } catch (IncorrectResultSizeDataAccessException e) {
            // Ветка обсуждения отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread " + slugOrId));
        } catch (PostService.NoParentPostException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("At least one parent post is missing in the current thread."));
        }
    }


    // Получение списка сообщений в данной ветке форуме..
    @GetMapping(path = "/posts", produces = "application/json")
    public ResponseEntity posts(@PathVariable("slug_or_id") String slugOrId,
                                @RequestParam(value="sort", required=false, defaultValue="flat") String sort,
                                @RequestParam(value="limit", required=false) Long limit) {
        try {
            return ResponseEntity.ok(postService.threadPosts(slugOrId));
        } catch (IndexOutOfBoundsException e) {
            // Ветка обсуждения отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread " + slugOrId));
        }
    }


    // Получение списка сообщений в данной ветке форуме..
    @PostMapping(path = "/vote", consumes = "application/json", produces = "application/json")
    public ResponseEntity vote(@PathVariable("slug_or_id") String slugOrId, @RequestBody Vote body) {
        try {
            return ResponseEntity.ok(voteService.vote(slugOrId, body));
        } catch (IncorrectResultSizeDataAccessException e) {
            // Ветка обсуждения отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find thread " + slugOrId));
        } catch (DataIntegrityViolationException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find user " + body.getNickname()));
        }
    }
}
