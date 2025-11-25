package trinhnv.springRestfull.service;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import trinhnv.springRestfull.config.TokenConfig;
import trinhnv.springRestfull.domain.dto.LoginDTO;
import trinhnv.springRestfull.domain.dto.RefreshTokenRequestDTO;
import trinhnv.springRestfull.domain.dto.ResLoginDTO;
import trinhnv.springRestfull.domain.entity.User;
import trinhnv.springRestfull.util.SecurityUtil;
import trinhnv.springRestfull.util.constant.TokenConstant;

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
 * Features:
 * - Login: Xác thực credentials, trả về Access Token + Refresh Token (JWT)
 * - Refresh: Verify refresh token JWT, tạo tokens mới
 * 
 * LIMITATIONS (do stateless):
 * - Không thể revoke token
 * - Logout không thực sự invalidate token
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
     * Flow:
     * 1. Validate credentials với AuthenticationManager
     * 2. Lấy user từ database
     * 3. Tạo Access Token (JWT, 15 phút)
     * 4. Tạo Refresh Token (JWT, 7 ngày)
     * 5. Trả về response
     * 
     * @param loginDTO Login credentials
     * @param request HTTP request
     * @return ResLoginDTO với tokens
     */
    @Transactional(readOnly = true)
    public ResLoginDTO login(LoginDTO loginDTO, HttpServletRequest request) {
        log.info("Login attempt for user: {}", loginDTO.getUsername());
        
        // 1. Authenticate với Spring Security
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                        loginDTO.getUsername(),
                        loginDTO.getPassword()
                );
        Authentication authentication = authenticationManager.authenticate(authToken);

        // 2. Get user entity
        User user = userService.hanldeUser(loginDTO.getUsername());

        // 3. Create Access Token (JWT)
        String accessToken = securityUtil.createAccessToken(authentication);

        // 4. Create Refresh Token (JWT) - STATELESS, không lưu DB
        String refreshToken = securityUtil.createRefreshToken(user);

        log.info("Login successful for user: {}", loginDTO.getUsername());

        // 5. Build response
        return ResLoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenConfig.getAccessTokenExpiration())
                .refreshExpiresIn(tokenConfig.getRefreshTokenExpiration())
                .tokenType(TokenConstant.TOKEN_TYPE)
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
     * Flow:
     * 1. Verify refresh token JWT (signature + expiration)
     * 2. Lấy username từ JWT claims
     * 3. Query user từ database
     * 4. Tạo access token mới
     * 5. Tạo refresh token mới
     * 6. Trả về tokens mới
     * 
     * NOTE: Stateless nên không có token rotation hay revocation
     * 
     * @param requestDTO Refresh token request
     * @param httpRequest HTTP request
     * @return ResLoginDTO với tokens mới
     */
    @Transactional(readOnly = true)
    public ResLoginDTO refreshToken(RefreshTokenRequestDTO requestDTO, HttpServletRequest httpRequest) {
        log.debug("Refresh token request received");
        
        // 1. Verify refresh token JWT
        String username = securityUtil.getUsernameFromRefreshToken(requestDTO.getRefreshToken());

        // 2. Query user từ database
        User user = userService.hanldeUser(username);
        if (user == null) {
            throw new RuntimeException("User không tồn tại");
        }

        // 3. Create new Access Token
        String accessToken = securityUtil.createAccessTokenFromUser(user);

        // 4. Create new Refresh Token
        String refreshToken = securityUtil.createRefreshToken(user);

        log.debug("Token refreshed for user: {}", username);

        // 5. Build response
        return ResLoginDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(tokenConfig.getAccessTokenExpiration())
                .refreshExpiresIn(tokenConfig.getRefreshTokenExpiration())
                .tokenType(TokenConstant.TOKEN_TYPE)
                .build();
    }

    /**
     * ===================================================================
     * LOGOUT (STATELESS - Limited functionality)
     * ===================================================================
     * 
     * ⚠️ STATELESS LIMITATION:
     * Với stateless JWT, logout KHÔNG THỂ invalidate token.
     * Token vẫn valid cho đến khi hết hạn.
     * 
     * Client-side phải:
     * - Xóa tokens khỏi storage
     * - Không gửi token trong requests tiếp theo
     * 
     * @param refreshToken Refresh token (không dùng trong stateless)
     */
    public void logout(String refreshToken) {
        log.info("Logout request received (stateless - client should remove tokens)");
        // Stateless: Không làm gì ở server
        // Client phải tự xóa tokens khỏi storage
    }

    /**
     * ===================================================================
     * GET CURRENT USER FROM TOKEN
     * ===================================================================
     * 
     * Lấy user từ username trong JWT token.
     * 
     * @param username Username từ JWT subject
     * @return User entity
     */
    public User getCurrentUser(String username) {
        return userService.hanldeUser(username);
    }
}
