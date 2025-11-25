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
import org.springframework.security.web.SecurityFilterChain;
import trinhnv.springRestfull.util.SecurityUtil;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


@Configuration
@EnableWebSecurity  // Bật Spring Security cho ứng dụng web
@EnableMethodSecurity(securedEnabled = true)  // Cho phép sử dụng @Secured annotation trên method
public class SecurityConfiguration {
    

    @Value("${trinhnguyen.jwtKey}")
    private String jwtSecretKey;
    

    @Value("${trinhnguyen.jwtSecond}")
    private String jwtSecond;


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }


    private SecretKey getSecretKey() {
        // Giải mã Base64 string thành byte array
        byte[] keyBytes = Base64.from(jwtSecretKey).decode();
        // Tạo SecretKey với thuật toán HS512 (từ SecurityUtil.JWT_ALGORITHM)
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, SecurityUtil.JWT_ALGORITHM.getName());
    }
    

    @Bean
    public JwtEncoder jwtEncoder() {
        return new NimbusJwtEncoder(new ImmutableSecret<>(getSecretKey()));
    }


    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CustomAuthenticationEntryPoint customAuthenticationEntryPoint
            ) throws Exception {
        http

                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeHttpRequests(authz -> authz
                        .requestMatchers("/","/login","/register").permitAll()
                        
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
//                .exceptionHandling(
//                        exceptions -> exceptions
//                                .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // 401
//                                .accessDeniedHandler(new BearerTokenAccessDeniedHandler()) // 403
//                )
                
                // ============================================================
                // SESSION MANAGEMENT - STATELESS (KHÔNG DÙNG SESSION)
                // ============================================================
                // Vì dùng JWT (stateless), không cần tạo session
                // Mỗi request phải gửi kèm token trong header
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }


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
