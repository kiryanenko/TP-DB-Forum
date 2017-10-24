package application.controllers;

import application.models.User;
import application.servicies.UserService;
import application.views.MessageResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@CrossOrigin
@RequestMapping(path = "/api/user/{nickname}")
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
        try {
            return ResponseEntity.status(HttpStatus.CREATED).body(userService.create(body));
        } catch (DuplicateKeyException e) {
            // Пользователь уже присутсвует в базе данных.
            // Возвращает данные ранее созданных пользователей с тем же nickname-ом иои email-ом.
            return ResponseEntity.status(HttpStatus.CONFLICT).body(userService.findSameUsers(body));
        }
    }


    // Получение информации о пользователе форума по его имени.
    @GetMapping(path = "/profile", produces = "application/json")
    public ResponseEntity profile(@PathVariable String nickname) {
        try {
            final User user = userService.findUserByNickname(nickname);
            return ResponseEntity.ok(user);
        }catch (IncorrectResultSizeDataAccessException e){
            // Пользователь отсутсвует в системе.
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find user with nickname " + nickname));
        }
    }


    @PostMapping(path = "/profile", consumes = "application/json", produces = "application/json")
    public ResponseEntity edit(@RequestBody User body, @PathVariable String nickname) {
        body.setNickname(nickname);
        try {
            final User updatedUser = userService.update(body);
            return ResponseEntity.ok(updatedUser);
        } catch (DuplicateKeyException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(new MessageResponse("Новые данные профиля пользователя конфликтуют с имеющимися пользователями."));
        } catch (IncorrectResultSizeDataAccessException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse("Can't find user with nickname " + nickname));
        }
    }
}
