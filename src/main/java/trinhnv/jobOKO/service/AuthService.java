package trinhnv.jobOKO.service;

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
import trinhnv.jobOKO.domain.request.LoginDTO;
import trinhnv.jobOKO.domain.request.LoginResult;
import trinhnv.jobOKO.domain.response.ResLoginDTO;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.util.SecurityUtil;

/**
 * ===================================================================
 * AUTH SERVICE (STATELESS)
 * ===================================================================
 * 
 * Service xử lý authentication với JWT tokens theo hướng STATELESS.
 * 
 * STATELESS APPROACH:
 * - Cả Access Token và Refresh Token đều là JWT
 * - KHÔNG lưu token vào database
 * - Verify token bằng signature
 * 
 * TOKEN STORAGE:
 * - Access Token: Trả về trong response body
 * - Refresh Token: Set vào httpOnly cookie (do Controller xử lý)
 * 
 * @author trinhnv
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final TokenConfig tokenConfig;

    /**
     * ===================================================================
     * LOGIN
     * ===================================================================
     * 
     * Xác thực user và trả về Access Token + Refresh Token.
     * 
     * @param loginDTO Login credentials
     * @param request HTTP request
     * @return LoginResult chứa cả accessToken và refreshToken
     */
    @Transactional(readOnly = true)
    public LoginResult login(LoginDTO loginDTO, HttpServletRequest request) {
        log.info("Login attempt | username={}", loginDTO.getUsername());
        
        Authentication authentication;
        try {
            // 1. Authenticate với Spring Security
            UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword()
                    );
            authentication = authenticationManager.authenticate(authToken);
            log.debug("Authenticate success | username={}", loginDTO.getUsername());
        } catch (Exception ex) {
            log.error("Authenticate failed | username={} | reason={}",
                    loginDTO.getUsername(), ex.getMessage(), ex);
            throw ex;
        }

        // 2. Get user entity
        User user = userService.hanldeUser(loginDTO.getUsername());
        if (user == null) {
            log.error("User not found after authentication | username={}", loginDTO.getUsername());
            throw new RuntimeException("User không tồn tại");
        }

        // 3. Create Access Token (JWT)
        String accessToken = securityUtil.createAccessToken(authentication);

        // 4. Create Refresh Token (JWT)
        String refreshToken = securityUtil.createRefreshToken(user);

        log.info("Login successful | username={}", loginDTO.getUsername());

        // 5. Build result (Controller sẽ set refreshToken vào cookie)
        return LoginResult.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenConfig.getAccessTokenExpiration())
                .refreshExpiresIn(tokenConfig.getRefreshTokenExpiration())
                .user(ResLoginDTO.UserInfo.builder()
                        .id(user.getId())
                        .username(user.getUserName())
                        .email(user.getEmail())
                        .build())
                .build();
    }

    /**
     * ===================================================================
     * REFRESH TOKEN
     * ===================================================================
     * 
     * Verify refresh token JWT và tạo tokens mới.
     * 
     * @param refreshToken Refresh token từ cookie
     * @return LoginResult với tokens mới
     */
    @Transactional(readOnly = true)
    public LoginResult refreshToken(String refreshToken) {
        log.debug("Refresh token request received");
        
        // 1. Verify refresh token JWT
        String username;
        try {
            username = securityUtil.getUsernameFromRefreshToken(refreshToken);
        } catch (Exception ex) {
            log.error("Refresh token invalid | reason={}", ex.getMessage(), ex);
            throw ex;
        }

        // 2. Query user từ database
        User user = userService.hanldeUser(username);
        if (user == null) {
            log.error("Refresh token references non-existing user | username={}", username);
            throw new RuntimeException("User không tồn tại");
        }

        // 3. Create new Access Token
        String newAccessToken = securityUtil.createAccessTokenFromUser(user);

        // 4. Create new Refresh Token
        String newRefreshToken = securityUtil.createRefreshToken(user);

        log.debug("Token refreshed | username={}", username);

        // 5. Build result
        return LoginResult.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(tokenConfig.getAccessTokenExpiration())
                .refreshExpiresIn(tokenConfig.getRefreshTokenExpiration())
                .build();
    }

    /**
     * ===================================================================
     * LOGOUT
     * ===================================================================
     * 
     * Với stateless JWT, server chỉ cần xóa cookie.
     * Token vẫn valid đến khi hết hạn nhưng client không có nữa.
     */
    public void logout() {
        log.info("Logout request received");
        // Cookie sẽ được xóa ở Controller
    }

    /**
     * Get current user from username
     */
    public User getCurrentUser(String username) {
        return userService.hanldeUser(username);
    }
}
