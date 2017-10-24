package application.controllers;

import application.models.Post;
import application.servicies.PostService;
import application.views.MessageResponse;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/post/{id}")
public class PostController {
    private PostService postService;


    public PostController(PostService postService) {
        this.postService = postService;
    }


    // Получение информации о ветке обсуждения по его имени.
    @GetMapping(path = "/details", consumes = "application/json", produces = "application/json")
    public ResponseEntity details(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(postService.postFull(id));
        } catch (IncorrectResultSizeDataAccessException e) {
            // Пост не найден.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find post with id = " + id));
        }
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
