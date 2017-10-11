package application.controllers;

import application.models.Forum;
import application.models.User;
import application.servicies.ForumService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(path = "/forum")
public class ForumController {
    private ForumService forumService;


    @Autowired
    public  ForumController(ForumService forumService) {
        this.forumService = forumService;
    }


    // Создание нового форума.
    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity create(@RequestBody Forum body) {
        try {
            final Forum createdForum = forumService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);
        } catch (IndexOutOfBoundsException e) {
            // Пользователь отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MessageResponse("Can't find user with nickname " + body.getUserNickname())
            );
        } catch (DataAccessException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumService.findForumBySlug(body.getSlug()));
        }

    }
}
