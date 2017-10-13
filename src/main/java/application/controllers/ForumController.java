package application.controllers;

import application.models.Thread;
import application.models.Forum;
import application.servicies.ForumService;
import application.servicies.ThreadService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/forum")
public class ForumController {
    private ForumService forumService;
    private ThreadService threadService;


    @Autowired
    public  ForumController(ForumService forumService, ThreadService threadService) {
        this.forumService = forumService;
        this.threadService = threadService;
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
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumService.findForumBySlug(body.getSlug()));
        }
    }


    // Получение информации о форуме по его идентификаторе.
    @GetMapping(path = "/{slug}/details", consumes = "application/json", produces = "application/json")
    public ResponseEntity details(@PathVariable String slug) {
        try {
            final Forum forum = forumService.findForumBySlug(slug);
            return ResponseEntity.ok(forum);
        } catch (IndexOutOfBoundsException e) {
            // Форум отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MessageResponse("Can't find forum with slug " + slug)
            );
        }
    }


    // Добавление новой ветки обсуждения на форум
    @PostMapping(path = "/{slug}/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity createThread(@PathVariable String slug, @RequestBody Thread body) {
        try {
            body.setForum(slug);
            final Thread createdThread = threadService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdThread);
        } catch (IndexOutOfBoundsException e) {
            // Автор ветки или форум не найдены.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find forum or author"));
        } catch (DuplicateKeyException e) {
            // Ветка обсуждения уже присутсвует в базе данных. Возвращает данные ранее созданной ветки обсуждения.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadService.findThreadBySlug(body.getSlug()));
        }
    }


    // Получение списка ветвей обсужления данного форума.
    // Ветви обсуждения выводятся отсортированные по дате создания.
    @GetMapping(path = "/{slug}/threads", consumes = "application/json", produces = "application/json")
    public ResponseEntity forumThreads(@PathVariable String slug) {
        try {
            return ResponseEntity.ok(threadService.forumThreads(slug));
        } catch (IndexOutOfBoundsException e) {
            // Форум не найден.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find forum " + slug));
        }
    }
}
