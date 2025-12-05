package trinhnv.jobOKO.config.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * ===================================================================
 * TOKEN CONFIGURATION
 * ===================================================================
 * 
 * Đọc cấu hình token từ application.properties
 * 
 * Properties:
 * - trinhnguyen.access-token-expiration: Thời gian sống Access Token (giây)
 * - trinhnguyen.refresh-token-expiration: Thời gian sống Refresh Token (giây)
 * - trinhnguyen.max-active-sessions: Số lượng tối đa sessions cho mỗi user
 * 
 * @author trinhnv
 */
@Configuration
@ConfigurationProperties(prefix = "trinhnguyen")
@Getter
@Setter
public class TokenConfig {

    /**
     * Access Token expiration time (seconds)
     * Default: 900 (15 phút)
     */
    private long accessTokenExpiration = 900;

    /**
     * Refresh Token expiration time (seconds)
     * Default: 604800 (7 ngày)
     */
    private long refreshTokenExpiration = 604800;

    /**
     * Max active sessions per user
     * Default: 5
     */
    private int maxActiveSessions = 5;

    /**
     * JWT Secret Key (Base64 encoded)
     */
    private String jwtKey;
}

