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


    @PostMapping(path = "/create", consumes = "application/json", produces = "application/json")
    public ResponseEntity create(@RequestBody User user, @PathVariable String nickname) {
        System.out.println("creat");
        user.setNickname(nickname);
        if (!userService.create(user)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(userService.findSameUser(user));
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(new MessageResponse("Successfully registered user"));
    }


    @GetMapping(path = "/profile", produces = "application/json")
    public ResponseEntity profile(@PathVariable String nickname) {
        return ResponseEntity.ok(nickname);
    }


    @PostMapping(path = "/profile", consumes = "application/json", produces = "application/json")
    public ResponseEntity edit(@RequestBody User body, @PathVariable String nickname) {
        return ResponseEntity.ok(new MessageResponse("Edit complite."));
    }
}
