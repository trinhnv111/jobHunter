package trinhnv.springRestfull.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.springRestfull.common.annotation.ApiMessage;
import trinhnv.springRestfull.config.TokenConfig;
import trinhnv.springRestfull.domain.dto.*;
import trinhnv.springRestfull.domain.entity.ApiResponse;
import trinhnv.springRestfull.service.AuthService;
import trinhnv.springRestfull.service.UserService;
import trinhnv.springRestfull.util.error.InvalidTokenException;

/**
 * ===================================================================
 * AUTH CONTROLLER (STATELESS)
 * ===================================================================
 * 
 * Controller xử lý authentication endpoints theo hướng STATELESS.
 * 
 * TOKEN STORAGE:
 * - Access Token: Trả về trong response body
 * - Refresh Token: Set vào httpOnly cookie (an toàn, không bị XSS)
 * 
 * Endpoints:
 * - POST /auth/login     : Đăng nhập
 * - POST /auth/register  : Đăng ký user mới
 * - POST /auth/refresh   : Refresh tokens (đọc RT từ cookie)
 * - POST /auth/logout    : Logout (xóa cookie)
 * 
 * @author trinhnv
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    private final UserService userService;

    /**
     * Cookie name for refresh token
     */
    private static final String REFRESH_TOKEN_COOKIE = "refresh_token";

    /**
     * ===================================================================
     * LOGIN
     * ===================================================================
     * 
     * POST /auth/login
     * 
     * Response:
     * - Body: { access_token, expires_in, token_type, user }
     * - Cookie: refresh_token (httpOnly, secure, sameSite=Strict)
     */
    @PostMapping("/login")
    @ApiMessage("Đăng nhập thành công")
    public ResponseEntity<ResLoginDTO> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpServletRequest request,
            HttpServletResponse response) {
        
        // 1. Authenticate và lấy tokens
        LoginResult result = authService.login(loginDTO, request);
        
        // 2. Set refresh token vào httpOnly cookie
        ResponseCookie refreshCookie = createRefreshTokenCookie(
                result.getRefreshToken(), 
                result.getRefreshExpiresIn()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        
        // 3. Trả về response (chỉ có access token, không có refresh token)
        return ResponseEntity.ok(result.toResLoginDTO());
    }

    /**
     * ===================================================================
     * REGISTER
     * ===================================================================
     */
    @PostMapping("/register")
    @ApiMessage("Đăng ký tài khoản thành công")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        UserDTO userDTO = userService.handleCreateRegisterUser(registerDTO);
        return ResponseEntity.ok(userDTO);
    }

    /**
     * ===================================================================
     * REFRESH TOKEN
     * ===================================================================
     * 
     * POST /auth/refresh
     * 
     * Đọc refresh token từ cookie, tạo tokens mới.
     * 
     * Response:
     * - Body: { access_token, expires_in, token_type }
     * - Cookie: refresh_token (mới)
     */
    @PostMapping("/refresh")
    @ApiMessage("Làm mới token thành công")
    public ResponseEntity<ResLoginDTO> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE, required = false) String refreshToken,
            HttpServletResponse response) {
        
        // 1. Kiểm tra refresh token từ cookie
        if (refreshToken == null || refreshToken.isEmpty()) {
            throw new InvalidTokenException("Refresh token không tồn tại. Vui lòng đăng nhập lại.");
        }
        
        // 2. Verify và tạo tokens mới
        LoginResult result = authService.refreshToken(refreshToken);
        
        // 3. Set refresh token mới vào cookie
        ResponseCookie newRefreshCookie = createRefreshTokenCookie(
                result.getRefreshToken(),
                result.getRefreshExpiresIn()
        );
        response.addHeader(HttpHeaders.SET_COOKIE, newRefreshCookie.toString());
        
        // 4. Trả về access token mới
        return ResponseEntity.ok(result.toResLoginDTO());
    }

    /**
     * ===================================================================
     * LOGOUT
     * ===================================================================
     * 
     * POST /auth/logout
     * 
     * Xóa refresh token cookie.
     * Access token vẫn valid đến khi hết hạn (stateless limitation).
     */
    @PostMapping("/logout")
    @ApiMessage("Đăng xuất thành công")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        
        // Xóa refresh token cookie
        ResponseCookie deleteCookie = ResponseCookie.from(REFRESH_TOKEN_COOKIE, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)  // Xóa cookie
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, deleteCookie.toString());
        
        authService.logout();
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .statusCode(200)
                        .message("Đăng xuất thành công")
                        .build()
        );
    }

    /**
     * ===================================================================
     * HELPER: Tạo refresh token cookie
     * ===================================================================
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken, long maxAgeSeconds) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE, refreshToken)
                .httpOnly(true)           // Không thể access bằng JavaScript (chống XSS)
                .secure(true)             // Chỉ gửi qua HTTPS
                .path("/")                // Cookie valid cho tất cả paths
                .maxAge(maxAgeSeconds)    // Thời gian sống của cookie
                .sameSite("Strict")       // Chống CSRF
                .build();
    }
}
