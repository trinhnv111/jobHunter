package trinhnv.jobOKO.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.domain.projection.UserDetailsProjections;
import trinhnv.jobOKO.domain.request.RegisterDTO;
import trinhnv.jobOKO.domain.request.UserDTO;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;

public interface UserService {
    ResultPaginationResponse<UserDTO> getAllUsers(Specification<User> spec, Pageable pageable);

    UserDetailsProjections findUserById(Long id);

    UserDTO handleCretaeUser(UserDTO user);

    void deleteUserById(Long id);

    UserDTO handleUpdateUser(Long id, UserDTO user);

    User hanldeUser(String userName);

    UserDTO handleCreateRegisterUser(RegisterDTO registerDTO);
}
