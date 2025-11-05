package trinhnv.springRestfull.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import trinhnv.springRestfull.domain.User;
import trinhnv.springRestfull.service.UserService;
import trinhnv.springRestfull.service.error.IdInvalidException;

import java.util.List;

@RestController
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    private UserController(UserService userService,PasswordEncoder passwordEncoder) {

        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>>getAllUsers(){
          this.userService.getAllUsers();

          return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id)  throws IdInvalidException {
        if (id > 1500) {
            throw new IdInvalidException("Id không hợp lệ");
        }

        this.userService.findUserById(id);

            return ResponseEntity.status(HttpStatus.OK).body(this.userService.findUserById(id));
    }

    @PostMapping("/users")
    public ResponseEntity<User> postMethodName(@RequestBody User user) {

        String hardPassWord = this.passwordEncoder.encode(user.getPassWord());
        user.setPassWord(hardPassWord);

        User createUser = this.userService.handleCretaeUser(user);

        return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
    }
    @PutMapping("/users/{id}")
    public ResponseEntity<User> putMethodName(@PathVariable Long id, @RequestBody User user) {
         this.userService.handleUpdateUser(id, user);

         return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
         this.userService.deleteUserById(id);

         return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }



}
