package trinhnv.springRestfull.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import trinhnv.springRestfull.domain.ApiResponse;
import trinhnv.springRestfull.domain.dto.LoginDTO;
import trinhnv.springRestfull.domain.dto.ResLoginDTO;
import trinhnv.springRestfull.util.SecurityUtil;

/**
 * ===================================================================
 * AUTH CONTROLLER - XỬ LÝ ĐĂNG NHẬP VÀ TẠO TOKEN
 * ===================================================================
 * 
 * Controller này xử lý endpoint đăng nhập:
 * 1. Nhận username/password từ client
 * 2. Xác thực thông tin đăng nhập
 * 3. Tạo JWT token nếu xác thực thành công
 * 4. Trả về token cho client
 * 
 * LUỒNG XỬ LÝ:
 * Client → AuthController → AuthenticationManager → UserDetailCustorm
 * → UserService → Database → So sánh password → Tạo token → Trả về client
 * 
 * @author trinhnv
 */
@RestController
public class AuthController {

    /**
     * AuthenticationManager: Quản lý quá trình xác thực
     * - Tự động tìm UserDetailsService (UserDetailCustorm) trong Spring context
     * - Gọi loadUserByUsername() để lấy user từ database
     * - So sánh password bằng PasswordEncoder
     */
    private final AuthenticationManager authenticationManager;
    
    /**
     * SecurityUtil: Utility class để tạo JWT token
     * - Nhận Authentication object (sau khi xác thực thành công)
     * - Tạo JWT token với thông tin user và thời gian hết hạn
     */
    private final SecurityUtil securityUtil;

    /**
     * Constructor injection - Spring tự động inject dependencies
     * 
     * @param authenticationManager AuthenticationManager từ SecurityConfiguration
     * @param securityUtil SecurityUtil để tạo token
     */
    public AuthController(AuthenticationManager authenticationManager, SecurityUtil securityUtil) {
       this.authenticationManager = authenticationManager;
       this.securityUtil = securityUtil;
    }

    /**
     * ===================================================================
     * LOGIN ENDPOINT - XỬ LÝ ĐĂNG NHẬP
     * ===================================================================
     * 
     * Endpoint: POST /login
     * 
     * Request Body:
     * {
     *   "username": "trinhnv",
     *   "password": "123456"
     * }
     * 
     * Response (Success):
     * {
     *   "token": "eyJhbGciOiJIUzUxMiJ9..."
     * }
     * 
     * Response (Error):
     * - 400 Bad Request: Username/password không hợp lệ (validation)
     * - 401 Unauthorized: Username/password sai (BadCredentialsException)
     * 
     * LUỒNG XỬ LÝ CHI TIẾT:
     * 
     * Bước 1: Nhận LoginDTO từ client
     *   - @Valid: Kiểm tra validation (username/password không được rỗng)
     *   - Nếu validation fail → Throw MethodArgumentNotValidException
     *     → Xử lý bởi GlobalException.handleMethodArgumentNotValidException()
     * 
     * Bước 2: Tạo UsernamePasswordAuthenticationToken
     *   - Chứa username và password (plain text)
     *   - Đây là input cho AuthenticationManager
     * 
     * Bước 3: Gọi AuthenticationManager.authenticate()
     *   → AuthenticationManager tự động:
     *     a) Tìm UserDetailsService (UserDetailCustorm) trong Spring context
     *     b) Gọi UserDetailCustorm.loadUserByUsername(username)
     *     c) UserDetailCustorm gọi UserService → UserRepository → Database
     *     d) Lấy user từ database (password đã mã hóa BCrypt)
     *     e) So sánh password:
     *        - Mã hóa password từ request bằng BCrypt
     *        - So sánh với password trong database
     *     f) Nếu khớp → Trả về Authentication object
     *     g) Nếu không khớp → Throw BadCredentialsException
     *        → Xử lý bởi GlobalException.handleUserPrincipalNotFound()
     * 
     * Bước 4: Tạo JWT token
     *   - Gọi SecurityUtil.createToken(authentication)
     *   - Token chứa: username, thời gian tạo, thời gian hết hạn
     *   - Token được ký bằng secret key
     * 
     * Bước 5: Trả về token cho client
     *   - Client lưu token và dùng cho các request tiếp theo
     *   - Header: Authorization: Bearer <token>
     * 
     * @param loginDTO LoginDTO chứa username và password
     * @return ResponseEntity chứa ResLoginDTO với token
     * 
     * @throws BadCredentialsException nếu username/password sai
     * @throws MethodArgumentNotValidException nếu validation fail
     */
    @PostMapping("/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody LoginDTO loginDTO) {

        // ============================================================
        // BƯỚC 1: TẠO AUTHENTICATION TOKEN
        // ============================================================
        // UsernamePasswordAuthenticationToken là input cho AuthenticationManager
        // Chứa username và password (plain text) từ client
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(
                    loginDTO.getUsername(),  // Username từ request
                    loginDTO.getPassword()   // Password từ request (plain text)
                );

        // ============================================================
        // BƯỚC 2: XÁC THỰC NGƯỜI DÙNG
        // ============================================================
        // AuthenticationManager sẽ:
        // 1. Tìm UserDetailsService (UserDetailCustorm) trong Spring context
        // 2. Gọi loadUserByUsername(username) để lấy user từ database
        // 3. So sánh password bằng PasswordEncoder (BCrypt)
        // 4. Nếu thành công → Trả về Authentication object
        // 5. Nếu thất bại → Throw BadCredentialsException
        //    → Xử lý bởi GlobalException.handleUserPrincipalNotFound()
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // ============================================================
        // BƯỚC 3: TẠO JWT TOKEN
        // ============================================================
        // Nếu đến đây → Xác thực thành công
        // Tạo JWT token từ Authentication object
        // Token chứa: username, thời gian tạo, thời gian hết hạn
        String accessToken = this.securityUtil.createToken(authentication);

        // ============================================================
        // BƯỚC 4: TRẢ VỀ TOKEN CHO CLIENT
        // ============================================================
        // Client sẽ lưu token và dùng cho các request tiếp theo
        // Header: Authorization: Bearer <token>
        ResLoginDTO res = new ResLoginDTO();
        res.setToken(accessToken);

        return ResponseEntity.ok().body(res);
    }
}
