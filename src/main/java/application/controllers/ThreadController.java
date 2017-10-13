package application.controllers;

import application.models.Thread;
import application.servicies.ThreadService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/thread/{slug_or_id}")
public class ThreadController {
    private ThreadService threadService;


    @Autowired
    public ThreadController(ThreadService threadService) {
        this.threadService = threadService;
    }


    // Получение информации о ветке обсуждения по его имени.
    @GetMapping(path = "/details", consumes = "application/json", produces = "application/json")
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
}
