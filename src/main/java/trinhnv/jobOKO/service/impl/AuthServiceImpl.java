package trinhnv.jobOKO.service.impl;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trinhnv.jobOKO.config.security.TokenConfig;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.domain.request.LoginRequest;
import trinhnv.jobOKO.domain.request.LoginResult;
import trinhnv.jobOKO.domain.response.LoginResponse;
import trinhnv.jobOKO.service.AuthService;
import trinhnv.jobOKO.service.UserService;
import trinhnv.jobOKO.util.SecurityUtil;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final TokenConfig tokenConfig;

    @Override
    @Transactional(readOnly = true)
    public LoginResult login(LoginRequest loginRequest, HttpServletRequest request) {
        log.info("Login attempt | username={}", loginRequest.getUsername());

        Authentication authentication;
        try {
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    );
            authentication = authenticationManager.authenticate(authToken);
            log.debug("Authenticate success | username={}", loginRequest.getUsername());
        } catch (Exception ex) {
            log.error("Authenticate failed | username={} | reason={}",
                    loginRequest.getUsername(), ex.getMessage(), ex);
            throw ex;
        }

        User user = userService.hanldeUser(loginRequest.getUsername());
        if (user == null) {
            log.error("User not found after authentication | username={}", loginRequest.getUsername());
            throw new RuntimeException("User không tồn tại");
        }

        String accessToken = securityUtil.createAccessToken(authentication);
        String refreshToken = securityUtil.createRefreshToken(user);

        log.info("Login successful | username={}", loginRequest.getUsername());

        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenConfig.getAccessTokenExpiration())
                .refreshExpiresIn(tokenConfig.getRefreshTokenExpiration())
                .user(LoginResponse.UserInfo.builder()
                        .id(user.getUserId())
                        .username(user.getUserName())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResult refreshToken(String refreshToken) {
        log.debug("Refresh token request received");

        String username;
        try {
            username = securityUtil.getUsernameFromRefreshToken(refreshToken);
        } catch (Exception ex) {
            log.error("Refresh token invalid | reason={}", ex.getMessage(), ex);
            throw ex;
        }

        User user = userService.hanldeUser(username);
        if (user == null) {
            log.error("Refresh token references non-existing user | username={}", username);
            throw new RuntimeException("User không tồn tại");
        }

        String newAccessToken = securityUtil.createAccessTokenFromUser(user);
        String newRefreshToken = securityUtil.createRefreshToken(user);

        log.debug("Token refreshed | username={}", username);

        return LoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(tokenConfig.getAccessTokenExpiration())
                .refreshExpiresIn(tokenConfig.getRefreshTokenExpiration())
                .build();
    }

    @Override
    public void logout() {
        log.info("Logout request received");
    }

    @Override
    public User getCurrentUser(String username) {
        return userService.hanldeUser(username);
    }
}


