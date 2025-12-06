package trinhnv.jobOKO.controller;

import com.turkraft.springfilter.boot.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.common.annotation.ApiMessage;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.domain.projection.UserDetailsProjections;
import trinhnv.jobOKO.domain.request.UserRequest;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.domain.response.UserResponse;
import trinhnv.jobOKO.service.UserService;
import trinhnv.jobOKO.util.error.IdInvalidException;


@RestController
@RequiredArgsConstructor

public class UserController {
    private final UserService userService;

    @GetMapping("/users")
    @ApiMessage("Lây danh sách người dùng thành công")
    public ResponseEntity<ResultPaginationResponse<UserResponse>> getAllUsers(
            @Filter Specification<User> user ,
            Pageable pageable
            ) {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.getAllUsers(user,pageable));
    }

    @GetMapping("/users/{id}")
    @ApiMessage("Lây danh sách người dùng theo ID thành công")
    public ResponseEntity<UserDetailsProjections> getUser(@PathVariable Long id) throws IdInvalidException {
        return ResponseEntity.status(HttpStatus.OK).body(this.userService.findUserById(id));
    }

    @PostMapping("/users")
    @ApiMessage("Thêm người dùng thành công")

    public ResponseEntity<UserResponse> postMethodName(@RequestBody UserRequest request) {
        UserResponse createUser = this.userService.handleCretaeUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createUser);
    }

    @PutMapping("/users/{id}")
    @ApiMessage("Cập nhật người dùng thành công")

    public ResponseEntity<UserResponse> putMethodName(@PathVariable Long id, @RequestBody UserRequest request) {
        UserResponse userResponse = this.userService.handleUpdateUser(id, request);

        return ResponseEntity.status(HttpStatus.OK).body(userResponse);
    }

    @DeleteMapping("/users/{id}")
    @ApiMessage("Xóa người dùng thành công")

    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        this.userService.deleteUserById(id);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }


}
