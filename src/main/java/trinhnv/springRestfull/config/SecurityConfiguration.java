package trinhnv.springRestfull.config;

import com.nimbusds.jose.jwk.source.ImmutableSecret;
import com.nimbusds.jose.util.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import trinhnv.springRestfull.util.SecurityUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * ===================================================================
 * SECURITY CONFIGURATION - CẤU HÌNH BẢO MẬT CHO TOÀN BỘ ỨNG DỤNG
 * ===================================================================
 * 
 * Class này chịu trách nhiệm:
 * 1. Cấu hình Spring Security
 * 2. Tạo các Bean cần thiết cho JWT (JwtEncoder, JwtDecoder)
 * 3. Cấu hình PasswordEncoder (BCrypt)
 * 4. Cấu hình AuthenticationManager
 * 5. Cấu hình SecurityFilterChain (quy tắc bảo mật cho các endpoint)
 * 6. Xử lý exception khi xác thực thất bại (401, 403)
 * 
 * @author trinhnv
 */
@Configuration
@EnableWebSecurity  // Bật Spring Security cho ứng dụng web
@EnableMethodSecurity(securedEnabled = true)  // Cho phép sử dụng @Secured annotation trên method
public class SecurityConfiguration {
    
    /**
     * ===================================================================
     * SECRET KEY - CHUỖI BÍ MẬT ĐỂ KÝ VÀ XÁC THỰC JWT TOKEN
     * ===================================================================
     * 
     * Secret key được lấy từ application.properties
     * - Dùng để KÝ (sign) token khi tạo
     * - Dùng để XÁC THỰC (verify) token khi nhận request
     * 
     * ⚠️ LƯU Ý: Secret key PHẢI giữ bí mật, không được commit lên Git!
     */
    @Value("${trinhnguyen.jwtKey}")
    private String jwtSecretKey;
    
    /**
     * ===================================================================
     * JWT EXPIRATION TIME - THỜI GIAN SỐNG CỦA TOKEN (giây)
     * ===================================================================
     * 
     * Ví dụ: 86400 giây = 24 giờ
     * Sau thời gian này, token sẽ hết hạn và user phải đăng nhập lại
     */
    @Value("${trinhnguyen.jwtSecond}")
    private String jwtSecond;

    /**
     * ===================================================================
     * PASSWORD ENCODER - MÃ HÓA PASSWORD BẰNG BCRYPT
     * ===================================================================
     * 
     * BCrypt là thuật toán mã hóa một chiều (one-way hashing):
     * - Mã hóa password trước khi lưu vào database
     * - So sánh password khi đăng nhập (không thể giải mã ngược)
     * - Tự động thêm salt để tăng tính bảo mật
     * 
     * @return PasswordEncoder instance sử dụng BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * ===================================================================
     * AUTHENTICATION MANAGER - QUẢN LÝ QUÁ TRÌNH XÁC THỰC
     * ===================================================================
     * 
     * AuthenticationManager là trung tâm xử lý xác thực:
     * 1. Nhận UsernamePasswordAuthenticationToken từ AuthController
     * 2. Tìm UserDetailsService (UserDetailCustorm) trong Spring context
     * 3. Gọi loadUserByUsername() để lấy thông tin user từ database
     * 4. So sánh password bằng PasswordEncoder
     * 5. Trả về Authentication object nếu thành công
     * 
     * @param config AuthenticationConfiguration từ Spring Security
     * @return AuthenticationManager instance
     * @throws Exception nếu cấu hình không hợp lệ
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * ===================================================================
     * GET SECRET KEY - CHUYỂN ĐỔI SECRET KEY TỪ STRING SANG SECRETKEY
     * ===================================================================
     * 
     * Secret key trong application.properties là chuỗi Base64
     * Cần chuyển đổi sang SecretKey object để sử dụng với JWT
     * 
     * @return SecretKey object dùng để ký và xác thực JWT
     */
    private SecretKey getSecretKey() {
        // Giải mã Base64 string thành byte array
        byte[] keyBytes = Base64.from(jwtSecretKey).decode();
        // Tạo SecretKey với thuật toán HS512 (từ SecurityUtil.JWT_ALGORITHM)
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }
    
    /**
     * ===================================================================
     * JWT ENCODER - TẠO VÀ MÃ HÓA JWT TOKEN
     * ===================================================================
     * 
     * JwtEncoder được sử dụng trong SecurityUtil.createToken():
     * 1. Nhận JwtClaimsSet (thông tin trong token: username, thời gian hết hạn...)
     * 2. Mã hóa và ký token bằng secret key
     * 3. Trả về chuỗi token (JWT string)
     * 
     * @return JwtEncoder instance sử dụng NimbusJwtEncoder
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }

    /**
     * ===================================================================
     * SECURITY FILTER CHAIN - CẤU HÌNH QUY TẮC BẢO MẬT CHO CÁC ENDPOINT
     * ===================================================================
     * 
     * Đây là phần QUAN TRỌNG NHẤT - định nghĩa:
     * - Endpoint nào được phép truy cập không cần token (permitAll)
     * - Endpoint nào yêu cầu token (authenticated)
     * - Cách xử lý exception khi xác thực thất bại
     * 
     * LUỒNG XỬ LÝ:
     * 1. Request đến → Kiểm tra authorizeHttpRequests
     * 2. Nếu permitAll → Cho phép truy cập
     * 3. Nếu authenticated → Kiểm tra JWT token trong header
     * 4. Nếu token hợp lệ → Cho phép truy cập
     * 5. Nếu token không hợp lệ → Gọi exceptionHandling
     * 
     * @param http HttpSecurity object để cấu hình
     * @param customAuthenticationEntryPoint Custom handler cho lỗi 401 (token không hợp lệ)
     * @return SecurityFilterChain đã được cấu hình
     * @throws Exception nếu cấu hình không hợp lệ
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint
            ) throws Exception {
        http
                // ============================================================
                // CSRF PROTECTION - TẮT CSRF VÌ DÙNG JWT (STATELESS)
                // ============================================================
                // CSRF chỉ cần cho ứng dụng web có session
                // Vì dùng JWT (stateless) nên không cần CSRF protection
                .csrf(csrf -> csrf.disable())
                
                // ============================================================
                // CORS - CHO PHÉP CROSS-ORIGIN REQUESTS
                // ============================================================
                // Cho phép frontend từ domain khác gọi API
                .cors(Customizer.withDefaults())
                
                // ============================================================
                // AUTHORIZATION RULES - QUY TẮC PHÂN QUYỀN
                // ============================================================
                .authorizeHttpRequests(authz -> authz
                        // Endpoint công khai - KHÔNG CẦN TOKEN
                        .requestMatchers("/","/login").permitAll()
                        
                        // Tất cả endpoint khác - PHẢI CÓ TOKEN HỢP LỆ
                        .anyRequest().authenticated()
                )
                
                // ============================================================
                // OAUTH2 RESOURCE SERVER - CẤU HÌNH JWT AUTHENTICATION
                // ============================================================
                // Cấu hình Spring Security sử dụng JWT để xác thực
                // - JwtDecoder sẽ tự động giải mã và xác thực token
                // - CustomAuthenticationEntryPoint xử lý lỗi khi token không hợp lệ
                .oauth2ResourceServer((oauth2) -> oauth2.jwt(Customizer.withDefaults())
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                )
                
                // ============================================================
                // DISABLE FORM LOGIN & HTTP BASIC
                // ============================================================
                // Tắt form login và HTTP Basic vì dùng JWT
                .formLogin(f -> f.disable())
                .httpBasic(basic -> basic.disable())
                
                // ============================================================
                // EXCEPTION HANDLING - XỬ LÝ LỖI XÁC THỰC
                // ============================================================
                // authenticationEntryPoint: Xử lý lỗi 401 (Unauthorized)
                //   - Token không tồn tại
                //   - Token không hợp lệ
                //   - Token hết hạn
                // 
                // accessDeniedHandler: Xử lý lỗi 403 (Forbidden)
                //   - Token hợp lệ nhưng không có quyền truy cập
                //   - User không có role phù hợp
                .exceptionHandling(
                        exceptions -> exceptions
                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // 401
                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // 403
                )
                
                // ============================================================
                // SESSION MANAGEMENT - STATELESS (KHÔNG DÙNG SESSION)
                // ============================================================
                // Vì dùng JWT (stateless), không cần tạo session
                // Mỗi request phải gửi kèm token trong header
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    /**
     * ===================================================================
     * JWT DECODER - GIẢI MÃ VÀ XÁC THỰC JWT TOKEN
     * ===================================================================
     * 
     * JwtDecoder được Spring Security tự động sử dụng khi:
     * 1. Client gửi request với header: Authorization: Bearer <token>
     * 2. Spring Security filter chain gọi JwtDecoder để giải mã token
     * 3. Kiểm tra signature bằng secret key
     * 4. Kiểm tra thời gian hết hạn
     * 5. Nếu hợp lệ → Tạo Authentication object
     * 6. Nếu không hợp lệ → Throw exception → Gọi exceptionHandling
     * 
     * EXCEPTION HANDLING:
     * - JwtException: Token không hợp lệ, signature sai, format sai
     * - ExpiredJwtException: Token đã hết hạn
     * → Tất cả exception sẽ được xử lý bởi CustomAuthenticationEntryPoint
     * 
     * @return JwtDecoder instance với custom error handling
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        // Tạo JwtDecoder với secret key và thuật toán HS512
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(
                getSecretKey()).macAlgorithm(SecurityUtil.JWT_ALGORITHM).build();
        
        // Wrap JwtDecoder với custom error handling
        return token -> {
            try {
                // Giải mã và xác thực token
                return jwtDecoder.decode(token);
            } catch (Exception e) {
                // Log lỗi để debug (có thể thay bằng logger)
                System.out.println(">>> JWT error: " + e.getMessage());
                // Throw lại exception để Spring Security xử lý
                // → Sẽ trigger CustomAuthenticationEntryPoint
                throw e;
            }
        };
    }


    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new
                JwtGrantedAuthoritiesConverter();
        grantedAuthoritiesConverter.setAuthorityPrefix("");
        grantedAuthoritiesConverter.setAuthoritiesClaimName("trinhnv");
        JwtAuthenticationConverter jwtAuthenticationConverter = new
                JwtAuthenticationConverter();

        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
        return jwtAuthenticationConverter;
    }

}
