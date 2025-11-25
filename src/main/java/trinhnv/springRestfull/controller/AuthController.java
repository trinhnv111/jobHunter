package trinhnv.springRestfull.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import trinhnv.springRestfull.domain.dto.LoginDTO;
import trinhnv.springRestfull.domain.dto.RefreshTokenRequestDTO;
import trinhnv.springRestfull.domain.dto.RegisterDTO;
import trinhnv.springRestfull.domain.dto.ResLoginDTO;
import trinhnv.springRestfull.domain.dto.UserDTO;
import trinhnv.springRestfull.domain.entity.ApiResponse;
import trinhnv.springRestfull.service.AuthService;
import trinhnv.springRestfull.service.UserService;

/**
 * ===================================================================
 * AUTH CONTROLLER (STATELESS)
 * ===================================================================
 * 
 * Controller xử lý authentication endpoints theo hướng STATELESS.
 * 
 * Endpoints:
 * - POST /auth/login     : Đăng nhập, trả về Access Token + Refresh Token (JWT)
 * - POST /auth/register  : Đăng ký user mới
 * - POST /auth/refresh   : Refresh tokens
 * - POST /auth/logout    : Logout (client-side only)
 * 
 * STATELESS NOTES:
 * - Cả Access Token và Refresh Token đều là JWT
 * - Không lưu token vào database
 * - Logout không invalidate token (client phải xóa)
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
     * ===================================================================
     * LOGIN
     * ===================================================================
     * 
     * POST /auth/login
     * 
     * Request Body:
     * {
     *   "username": "string",
     *   "password": "string"
     * }
     * 
     * Response:
     * {
     *   "access_token": "eyJhbG...",      // JWT Access Token
     *   "refresh_token": "eyJhbG...",     // JWT Refresh Token  
     *   "expires_in": 900,
     *   "refresh_expires_in": 604800,
     *   "token_type": "Bearer",
     *   "user": { "id": 1, "username": "string", "email": "string" }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(
            @Valid @RequestBody LoginDTO loginDTO,
            HttpServletRequest request) {
        
        ResLoginDTO response = authService.login(loginDTO, request);
        return ResponseEntity.ok(response);
    }

    /**
     * ===================================================================
     * REGISTER
     * ===================================================================
     * 
     * POST /auth/register
     */
    @PostMapping("/register")
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
     * Verify JWT refresh token và tạo tokens mới.
     * 
     * Request Body:
     * {
     *   "refreshToken": "eyJhbG..."  // JWT refresh token
     * }
     * 
     * Response: Giống login response (tokens mới)
     */
    @PostMapping("/refresh")
    public ResponseEntity<ResLoginDTO> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDTO requestDTO,
            HttpServletRequest httpRequest) {
        
        ResLoginDTO response = authService.refreshToken(requestDTO, httpRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * ===================================================================
     * LOGOUT (STATELESS)
     * ===================================================================
     * 
     * POST /auth/logout
     * 
     * ⚠️ STATELESS LIMITATION:
     * Với JWT stateless, server KHÔNG THỂ invalidate token.
     * Endpoint này chỉ để client biết đã "logout".
     * 
     * Client PHẢI:
     * - Xóa access_token và refresh_token khỏi storage
     * - Không gửi tokens trong requests tiếp theo
     * 
     * Request Body:
     * {
     *   "refreshToken": "eyJhbG..."  // Optional
     * }
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestBody(required = false) RefreshTokenRequestDTO requestDTO) {
        
        // Stateless: Server không làm gì
        // Chỉ trả về response để client biết
        authService.logout(requestDTO != null ? requestDTO.getRefreshToken() : null);
        
        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .statusCode(200)
                        .message("Đăng xuất thành công. Vui lòng xóa tokens ở client.")
                        .build()
        );
    }
}
