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
import trinhnv.springRestfull.domain.dto.LoginDTO;
import trinhnv.springRestfull.domain.dto.ResLoginDTO;
import trinhnv.springRestfull.util.SecurityUtil;


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
        SecurityContextHolder.getContext().setAuthentication(authentication);
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
