package application.controllers;

import application.models.Thread;
import application.models.Forum;
import application.servicies.ForumService;
import application.servicies.ThreadService;
import application.servicies.UserService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/forum")
public class ForumController {
    private ForumService forumService;
    private ThreadService threadService;
    private UserService userService;


    @Autowired
    public  ForumController(ForumService forumService, ThreadService threadService, UserService userService) {
        this.forumService = forumService;
        this.threadService = threadService;
        this.userService = userService;
    }


    // Создание нового форума.
    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity create(@RequestBody Forum body) {
        try {
            final Forum createdForum = forumService.create(body);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdForum);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(forumService.findForumBySlug(body.getSlug()));
        } catch (DataIntegrityViolationException e) {
            // Пользователь отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new MessageResponse("Can't find user with nickname " + body.getUserNickname())
            );
        }
    }


    // Получение информации о форуме по его идентификаторе.
    @GetMapping(path = "/{slug}/details", produces = "application/json")
    public ResponseEntity details(@PathVariable String slug) {
        try {
            final Forum forum = forumService.findForumBySlug(slug);
            return ResponseEntity.ok(forum);
        } catch (IncorrectResultSizeDataAccessException e) {
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
        } catch (IncorrectResultSizeDataAccessException e) {
            // Автор ветки или форум не найдены.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find forum or author"));
        } catch (DuplicateKeyException e) {
            // Ветка обсуждения уже присутсвует в базе данных. Возвращает данные ранее созданной ветки обсуждения.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(threadService.findThreadBySlug(body.getSlug()));
        }
    }


    // Получение списка ветвей обсужления данного форума.
    // Ветви обсуждения выводятся отсортированные по дате создания.
    @GetMapping(path = "/{slug}/threads", produces = "application/json")
    public ResponseEntity forumThreads(@PathVariable String slug,
                                       @RequestParam(value="desc", required=false, defaultValue="false") Boolean desc,
                                       @RequestParam(value="limit", required=false) Long limit,
                                       @RequestParam(value="since", required=false) String sinceStr) {
        Date since = null;
        if (sinceStr != null) {
            try {
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");
                since = dateFormat.parse(sinceStr);
            } catch (ParseException e) {
                return ResponseEntity.badRequest().body("Bad since");
            }
        }

        try {
            return ResponseEntity.ok(threadService.forumThreads(slug, desc, limit, since));
        } catch (IncorrectResultSizeDataAccessException e) {
            // Форум не найден.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find forum " + slug));
        }
    }


    // Получение списка пользователей, у которых есть пост или ветка обсуждения в данном форуме.
    @GetMapping(path = "/{slug}/users", consumes = "application/json", produces = "application/json")
    public ResponseEntity forumUsers(@PathVariable String slug) {
        try {
            return ResponseEntity.ok(userService.forumUsers(slug));
        } catch (IncorrectResultSizeDataAccessException e) {
            // Форум не найден.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find forum " + slug));
        }
    }
}
