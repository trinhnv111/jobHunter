package trinhnv.springRestfull.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;


@Service
public class UserDetailCustorm implements UserDetailsService {
    private final UserService userService;
    public UserDetailCustorm(UserService userService) {
        this.userService = userService;
    }
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        trinhnv.springRestfull.domain.User user = this.userService.hanldeUser(username);

        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),
                user.getPassWord(),
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );
    }
}
