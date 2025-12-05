package trinhnv.jobOKO.util;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import trinhnv.jobOKO.config.security.TokenConfig;
import trinhnv.jobOKO.domain.entity.User;
import trinhnv.jobOKO.util.error.InvalidTokenException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.stream.Collectors;

/**
 * ===================================================================
 * SECURITY UTIL - JWT TOKEN UTILITY (STATELESS)
 * ===================================================================
 * 
 * Utility class để tạo và quản lý JWT Tokens theo hướng STATELESS.
 * 
 * STATELESS APPROACH:
 * - Cả Access Token và Refresh Token đều là JWT
 * - KHÔNG lưu token vào database
 * - Verify token bằng signature, không cần query DB
 * - Scalable, phù hợp microservices
 * 
 * TOKEN TYPES:
 * - Access Token: JWT, short-lived (15 phút), dùng cho API requests
 * - Refresh Token: JWT, long-lived (7 ngày), dùng để lấy access token mới
 * 
 * TRADE-OFFS:
 * - ❌ Không thể revoke token (phải đợi hết hạn)
 * - ❌ Logout không thực sự logout
 * - ✅ Không cần database cho tokens
 * - ✅ Scalable, stateless
 * 
 * @see TokenConfig
 * @author trinhnv
 */
@Service
@RequiredArgsConstructor
public class SecurityUtil {
    
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final TokenConfig tokenConfig;
    
    /**
     * JWT Algorithm: HS512 (HMAC-SHA512)
     */
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;

    // ===================================================================
    // ACCESS TOKEN
    // ===================================================================

    /**
     * Tạo Access Token từ Authentication (sau login)
     * 
     * @param authentication Authentication object từ AuthenticationManager
     * @return JWT access token string
     */
    public String createAccessToken(Authentication authentication) {
        Instant now = Instant.now();
        Instant validity = now.plus(tokenConfig.getAccessTokenExpiration(), ChronoUnit.SECONDS);

        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(authentication.getName())
                .claim("type", "access")
                .claim("authorities", authorities)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }

    /**
     * Tạo Access Token từ User entity (khi refresh)
     * 
     * @param user User entity
     * @return JWT access token string
     */
    public String createAccessTokenFromUser(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(tokenConfig.getAccessTokenExpiration(), ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getUserName())
                .claim("type", "access")
                .claim("userId", user.getId())
                .claim("email", user.getEmail())
                .claim("authorities", "ROLE_USER")
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }

    // ===================================================================
    // REFRESH TOKEN (JWT - STATELESS)
    // ===================================================================

    /**
     * Tạo Refresh Token (JWT)
     * 
     * Refresh Token cũng là JWT nhưng:
     * - Thời gian sống dài hơn (7 ngày)
     * - Chỉ chứa thông tin cần thiết (username, userId)
     * - Dùng để lấy access token mới
     * 
     * @param user User entity
     * @return JWT refresh token string
     */
    public String createRefreshToken(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(tokenConfig.getRefreshTokenExpiration(), ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getUserName())
                .claim("type", "refresh")  // Đánh dấu đây là refresh token
                .claim("userId", user.getId())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();

        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }

    /**
     * Verify và decode Refresh Token
     * 
     * @param refreshToken JWT refresh token string
     * @return Jwt object nếu valid
     * @throws InvalidTokenException nếu token không hợp lệ
     */
    public Jwt verifyRefreshToken(String refreshToken) {
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            
            // Kiểm tra type phải là "refresh"
            String tokenType = jwt.getClaim("type");
            if (!"refresh".equals(tokenType)) {
                throw new InvalidTokenException("Token không phải là refresh token");
            }
            
            return jwt;
        } catch (JwtException e) {
            throw new InvalidTokenException("Refresh token không hợp lệ: " + e.getMessage());
        }
    }

    /**
     * Lấy username từ Refresh Token
     * 
     * @param refreshToken JWT refresh token string
     * @return username
     */
    public String getUsernameFromRefreshToken(String refreshToken) {
        Jwt jwt = verifyRefreshToken(refreshToken);
        return jwt.getSubject();
    }

    /**
     * Lấy userId từ Refresh Token
     * 
     * @param refreshToken JWT refresh token string
     * @return userId
     */
    public Long getUserIdFromRefreshToken(String refreshToken) {
        Jwt jwt = verifyRefreshToken(refreshToken);
        return jwt.getClaim("userId");
    }

    // ===================================================================
    // UTILITY METHODS
    // ===================================================================

    /**
     * Lấy username từ JWT token
     */
    public String getUsernameFromToken(Jwt jwt) {
        return jwt.getSubject();
    }

    /**
     * Lấy claim từ JWT token
     */
    public Object getClaimFromToken(Jwt jwt, String claimName) {
        return jwt.getClaim(claimName);
    }

    /**
     * Get access token expiration (for response)
     */
    public long getAccessTokenExpiration() {
        return tokenConfig.getAccessTokenExpiration();
    }

    /**
     * Get refresh token expiration (for response)
     */
    public long getRefreshTokenExpiration() {
        return tokenConfig.getRefreshTokenExpiration();
    }

    /**
     * @deprecated Sử dụng createAccessToken() thay thế
     */
    @Deprecated
    public String createToken(Authentication authentication) {
        return createAccessToken(authentication);
    }
}
