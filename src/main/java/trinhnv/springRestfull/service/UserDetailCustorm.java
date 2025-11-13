package trinhnv.springRestfull.service;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * ===================================================================
 * USER DETAIL CUSTOM - PHẦN CUSTOM ĐỂ LOAD USER TỪ DATABASE ⭐
 * ===================================================================
 * 
 * ⭐ ĐÂY LÀ PHẦN QUAN TRỌNG NHẤT - PHẦN BẠN TỰ VIẾT!
 * 
 * Spring Security KHÔNG BIẾT cách lấy user từ database của bạn
 * → Bạn phải tự implement UserDetailsService để:
 *   1. Load user từ database theo username
 *   2. Trả về UserDetails object cho Spring Security
 * 
 * CÁCH SPRING SECURITY TÌM THẤY CLASS NÀY:
 * - @Service annotation đăng ký class này với Spring context
 * - Spring Security tự động tìm UserDetailsService trong context
 * - Khi AuthenticationManager.authenticate() được gọi:
 *   → Tự động gọi loadUserByUsername() của class này
 * 
 * LUỒNG XỬ LÝ:
 * AuthenticationManager.authenticate()
 *   → Tìm UserDetailsService trong Spring context
 *   → Tìm thấy UserDetailCustorm (class này)
 *   → Gọi loadUserByUsername(username)
 *   → loadUserByUsername() gọi UserService → UserRepository → Database
 *   → Trả về UserDetails
 *   → Spring Security so sánh password
 * 
 * @author trinhnv
 */
@Service
public class UserDetailCustorm implements UserDetailsService {
    
    /**
     * UserService: Service layer để truy vấn user từ database
     */
    private final UserService userService;
    
    /**
     * Constructor injection - Spring tự động inject UserService
     * 
     * @param userService UserService để truy vấn database
     */
    public UserDetailCustorm(UserService userService) {
        this.userService = userService;
    }
    
    /**
     * ===================================================================
     * LOAD USER BY USERNAME - LOAD USER TỪ DATABASE ⭐
     * ===================================================================
     * 
     * ⭐ ĐÂY LÀ PHẦN CUSTOM CỦA BẠN!
     * 
     * Method này được Spring Security tự động gọi khi:
     * - AuthenticationManager.authenticate() được gọi
     * - Cần lấy thông tin user từ database để xác thực
     * 
     * LUỒNG XỬ LÝ:
     * 
     * Bước 1: Lấy user từ database
     *   - Gọi UserService.hanldeUser(username)
     *   - UserService gọi UserRepository.findByUserName(username)
     *   - UserRepository query database: SELECT * FROM user WHERE user_name = ?
     * 
     * Bước 2: Kiểm tra user có tồn tại không
     *   - Nếu user == null → Throw UsernameNotFoundException
     *   - Exception này sẽ được xử lý bởi Spring Security
     *   - → Trả về 401 Unauthorized cho client
     * 
     * Bước 3: Tạo UserDetails object
     *   - UserDetails là interface của Spring Security
     *   - Chứa: username, password (đã mã hóa BCrypt), authorities (quyền)
     *   - Spring Security sẽ dùng password này để so sánh với password từ request
     * 
     * ⚠️ LƯU Ý QUAN TRỌNG:
     * - Password trong database PHẢI được mã hóa bằng BCrypt
     * - Nếu password chưa mã hóa → Xác thực sẽ thất bại
     * - Authorities (quyền) định nghĩa role của user
     *   Ví dụ: ROLE_USER, ROLE_ADMIN
     * 
     * @param username Username cần tìm
     * @return UserDetails object chứa thông tin user
     * @throws UsernameNotFoundException nếu không tìm thấy user
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        
        // ============================================================
        // BƯỚC 1: LẤY USER TỪ DATABASE
        // ============================================================
        // Gọi UserService → UserRepository → Database
        // Query: SELECT * FROM user WHERE user_name = ?
        trinhnv.springRestfull.domain.User user = this.userService.hanldeUser(username);

        // ============================================================
        // BƯỚC 2: KIỂM TRA USER CÓ TỒN TẠI KHÔNG
        // ============================================================
        // Nếu không tìm thấy user → Throw UsernameNotFoundException
        // Exception này sẽ được Spring Security xử lý
        // → Trả về 401 Unauthorized cho client
        if (user == null) {
            throw new UsernameNotFoundException("không tìm thấy: " + username);
        }

        // ============================================================
        // BƯỚC 3: TẠO USERDETAILS OBJECT
        // ============================================================
        // UserDetails là interface của Spring Security
        // Chứa thông tin user cần thiết để xác thực:
        // - username: Tên đăng nhập
        // - password: Password đã mã hóa BCrypt (từ database)
        // - authorities: Danh sách quyền (role) của user
        // 
        // Spring Security sẽ:
        // 1. Lấy password từ UserDetails (đã mã hóa BCrypt)
        // 2. Lấy password từ request (plain text)
        // 3. Mã hóa password từ request bằng BCryptPasswordEncoder
        // 4. So sánh 2 chuỗi đã mã hóa
        // 5. Nếu khớp → Xác thực thành công
        // 6. Nếu không khớp → Throw BadCredentialsException
        return new org.springframework.security.core.userdetails.User(
                user.getUserName(),                    // Username
                user.getPassWord(),                    // Password đã mã hóa BCrypt (từ database)
                Collections.singletonList(            // Authorities (quyền)
                    new SimpleGrantedAuthority("ROLE_USER")
                )
        );
    }
}
