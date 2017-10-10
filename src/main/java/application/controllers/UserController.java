package application.controllers;

import application.models.User;
import application.servicies.UserService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/user/{nickname}")
public class UserController {
    private UserService userService;


    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }


    // Создание нового пользователя в базе данных.
    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity create(@RequestBody User body, @PathVariable String nickname) {
        body.setNickname(nickname);
        final User createdUser = userService.create(body);
        if (createdUser == null) {
            // Пользователь уже присутсвует в базе данных.
            // Возвращает данные ранее созданных пользователей с тем же nickname-ом иои email-ом.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(userService.findSameUsers(body));
        }
        // Пользователь успешно создан. Возвращает данные созданного пользователя.
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    // Получение информации о пользователе форума по его имени.
    @GetMapping(path = "/profile", produces = "application/json")
    public ResponseEntity profile(@PathVariable String nickname) {
        final User user = userService.findUserByNickname(nickname);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find user with nickname " + nickname));
        }
        return ResponseEntity.ok(user);
    }


    @PostMapping(path = "/profile", consumes = "application/json", produces = "application/json")
    public ResponseEntity edit(@RequestBody User body, @PathVariable String nickname) {
        return ResponseEntity.ok(new MessageResponse("Edit complite."));
    }
}
