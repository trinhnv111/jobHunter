package trinhnv.springRestfull.service;

import org.springframework.stereotype.Service;

import org.springframework.web.bind.annotation.PathVariable;
import trinhnv.springRestfull.domain.User;
import trinhnv.springRestfull.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    // done
    public User findUserById(Long id){
        return userRepository.findById(id).orElse(null);
    }

    public User handleCretaeUser(User user) {
        return userRepository.save(user);
    }

    public void deleteUserById(Long id){
        userRepository.deleteById(id);
    }

    public User handleUpdateUser(Long id , User user) {
        User updatedUser = findUserById(id);

        if(updatedUser != null){
            updatedUser.setUserName(user.getUserName());
            updatedUser.setEmail(user.getEmail());
            updatedUser.setPassWord(user.getPassWord());
        }

        return userRepository.save(updatedUser);
    }

    public User hanldeUser(String userName){
        return userRepository.findByUserName(userName);
    }


}
