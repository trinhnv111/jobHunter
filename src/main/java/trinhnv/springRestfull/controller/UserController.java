package trinhnv.springRestfull.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.springRestfull.common.annotation.ApiMessage;
import trinhnv.springRestfull.domain.dto.UserDTO;
import trinhnv.springRestfull.service.UserService;
import trinhnv.springRestfull.util.error.IdInvalidException;

import java.util.List;

@RestController
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    @ApiMessage("Lây danh sách người dùng thành công")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUsers());
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Lây danh sách người dùng theo ID thành công")

    public ResponseEntity<UserDTO> getUser(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.findUserById(id));
    }

    @PostMapping("/users")
    @ApiMessage("Thêm người dùng thành công")

    public ResponseEntity<UserDTO> postMethodName(@RequestBody UserDTO user) {
        UserDTO createUser = this.userService.handleCretaeUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
    }

    @PutMapping("/users/{id}")
    @ApiMessage("Cập nhật người dùng thành công")

    public ResponseEntity<UserDTO> putMethodName(@PathVariable Long id, @RequestBody UserDTO user) {
        this.userService.handleUpdateUser(id, user);

        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Xóa người dùng thành công")

    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        this.userService.deleteUserById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
