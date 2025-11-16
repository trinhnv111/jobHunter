package trinhnv.springRestfull.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import trinhnv.springRestfull.domain.dto.UserDTO;
import trinhnv.springRestfull.domain.entity.User;
import trinhnv.springRestfull.domain.mapper.UserMapper;
import trinhnv.springRestfull.repository.UserRepository;
import trinhnv.springRestfull.util.response.ResultPaginationResponse;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository,UserMapper userMapper,PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public ResultPaginationResponse<UserDTO> getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> user = (spec!= null) ? this.userRepository.findAll(spec,pageable) : this.userRepository.findAll(pageable);

        return ResultPaginationResponse.ok(user,userMapper :: toDto);
    }

    public UserDTO findUserById(Long id){
        User user = userRepository.findById(id).orElseThrow(()-> new BadCredentialsException("Không tìm thấy người dùng"));

        return this.userMapper.toDto(user);
    }

    public UserDTO handleCretaeUser(UserDTO user) {
        String hardPassWord = this.passwordEncoder.encode(user.getPassWord());
        user.setPassWord(hardPassWord);

        User userEntity = userMapper.toEntity(user);
        return this.userMapper.toDto(userRepository.save(userEntity));
    }

    public void deleteUserById(Long id){
        userRepository.deleteById(id);
    }

    public UserDTO handleUpdateUser(Long id , UserDTO user) {

        // tìm -> chuyển dto sang enties -> lưu -> chuyển sang dto
        User updatedUser = this.userRepository.findById(id).orElseThrow(()->new BadCredentialsException("không tìm thấy ngời dùng"));
        userMapper.updateUserFromDTO(user, updatedUser);

        return userMapper.toDto(this.userRepository.save(updatedUser));
    }

    public User hanldeUser(String userName){
        return userRepository.findByUserName(userName);
    }


}
