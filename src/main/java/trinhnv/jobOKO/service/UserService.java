package trinhnv.jobOKO.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.domain.projection.UserDetailsProjections;
import trinhnv.jobOKO.domain.request.RegisterRequest;
import trinhnv.jobOKO.domain.request.UserRequest;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.domain.response.UserResponse;

public interface UserService {
    ResultPaginationResponse<UserResponse> getAllUsers(Specification<User> spec, Pageable pageable);

    UserDetailsProjections findUserById(Long id);

    UserResponse handleCretaeUser(UserRequest request);

    void deleteUserById(Long id);

    UserResponse handleUpdateUser(Long id, UserRequest request);

    User hanldeUser(String userName);

    UserResponse handleCreateRegisterUser(RegisterRequest registerRequest);
}
