package trinhnv.jobOKO.service.impl;

import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.domain.mapper.UserMapper;
import trinhnv.jobOKO.domain.projection.UserDetailsProjections;
import trinhnv.jobOKO.domain.request.RegisterRequest;
import trinhnv.jobOKO.domain.request.UserRequest;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.domain.response.UserResponse;
import trinhnv.jobOKO.repository.CompanyRespository;
import trinhnv.jobOKO.repository.UserRepository;
import trinhnv.jobOKO.service.UserService;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final CompanyRespository companyRespository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, CompanyRespository companyRespository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.companyRespository = companyRespository;
    }

    @Override
    public ResultPaginationResponse<UserResponse> getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> user = (spec != null) ? this.userRepository.findAll(spec, pageable) : this.userRepository.findAll(pageable);
        return ResultPaginationResponse.ok(user, userMapper::toResponse);
    }

    @Override
    public UserDetailsProjections findUserById(Long id) {
        UserDetailsProjections result = userRepository.findUserDetailsById(id);
        if (result == null || result.getUserId() == null) {
            throw new BadCredentialsException("Không tìm thấy người dùng");
        }
        return result;
    }

    @Override
    @Transactional
    public UserResponse handleCretaeUser(UserRequest request) {
        if (request.getCompanyId() != null) {
            boolean checkCompany = this.companyRespository.existsById(request.getCompanyId());
            if (!checkCompany) {
                throw new BadCredentialsException("Công ty không tồn tại!");
            }
        }
        String hardPassWord = this.passwordEncoder.encode(request.getPassWord());
        request.setPassWord(hardPassWord);

        User userEntity = userMapper.toEntity(request);

        return this.userMapper.toResponse(userRepository.save(userEntity));
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserResponse handleUpdateUser(Long id, UserRequest request) {
        User updatedUser = this.userRepository.findById(id).orElseThrow(() -> new BadCredentialsException("không tìm thấy ngời dùng"));

        userMapper.updateUserFromRequest(request, updatedUser);

        if (request.getPassWord() != null) {
            updatedUser.setPassWord(this.passwordEncoder.encode(request.getPassWord()));
        }

        return userMapper.toResponse(this.userRepository.save(updatedUser));
    }

    @Override
    public User hanldeUser(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public UserResponse handleCreateRegisterUser(RegisterRequest registerRequest) {
        if (this.userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (this.userRepository.existsByUserName(registerRequest.getUserName())) {
            throw new RuntimeException("Tài khoản đã tồn tại");
        }

        User user = userMapper.registerUser(registerRequest);
        user.setPassWord(passwordEncoder.encode(registerRequest.getPassword()));

        User userSave = this.userRepository.save(user);

        return this.userMapper.toResponse(userSave);
    }
}

