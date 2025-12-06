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
import trinhnv.jobOKO.domain.request.RegisterDTO;
import trinhnv.jobOKO.domain.request.UserDTO;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
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
    public ResultPaginationResponse<UserDTO> getAllUsers(Specification<User> spec, Pageable pageable) {
        Page<User> user = (spec != null) ? this.userRepository.findAll(spec, pageable) : this.userRepository.findAll(pageable);
        return ResultPaginationResponse.ok(user, userMapper::toDto);
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
    public UserDTO handleCretaeUser(UserDTO user) {
        if (user.getCompanyId() != null) {
            boolean checkCompany = this.companyRespository.existsById(user.getCompanyId());
            if (!checkCompany) {
                throw new BadCredentialsException("Công ty không tồn tại!");
            }
        }
        String hardPassWord = this.passwordEncoder.encode(user.getPassWord());
        user.setPassWord(hardPassWord);

        User userEntity = userMapper.toEntity(user);


        return this.userMapper.toDto(userRepository.save(userEntity));
    }

    @Override
    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDTO handleUpdateUser(Long id, UserDTO user) {
        User updatedUser = this.userRepository.findById(id).orElseThrow(() -> new BadCredentialsException("không tìm thấy ngời dùng"));

        userMapper.updateUserFromDTO(user, updatedUser);

        if (user.getPassWord() != null) {
            updatedUser.setPassWord(this.passwordEncoder.encode(user.getPassWord()));
        }

        return userMapper.toDto(this.userRepository.save(updatedUser));
    }

    @Override
    public User hanldeUser(String userName) {
        return userRepository.findByUserName(userName);
    }

    @Override
    public UserDTO handleCreateRegisterUser(RegisterDTO registerDTO) {
        if (this.userRepository.existsByEmail(registerDTO.getEmail())) {
            throw new RuntimeException("Email đã tồn tại");
        }
        if (this.userRepository.existsByUserName(registerDTO.getUserName())) {
            throw new RuntimeException("Tài khoản đã tồn tại");
        }

        User user = userMapper.registerUser(registerDTO);
        user.setPassWord(passwordEncoder.encode(registerDTO.getPassword()));

        User userSave = this.userRepository.save(user);

        return this.userMapper.toDto(userSave);
    }
}

