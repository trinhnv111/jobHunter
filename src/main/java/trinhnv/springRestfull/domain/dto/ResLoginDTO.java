package trinhnv.springRestfull.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * ===================================================================
 * LOGIN RESPONSE DTO
 * ===================================================================
 * 
 * Response trả về sau khi login/refresh thành công.
 * 
 * Bao gồm:
 * - Access Token: JWT, short-lived
 * - Refresh Token: UUID, long-lived
 * - Thông tin expiration
 * - User info (optional)
 * 
 * @author trinhnv
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResLoginDTO {

    /**
     * Access Token (JWT)
     * - Dùng để access protected resources
     * - Short-lived: 15 phút
     * - Gửi trong header: Authorization: Bearer <access_token>
     */
    @JsonProperty("access_token")
    private String accessToken;

    /**
     * Refresh Token (UUID)
     * - Dùng để lấy access token mới
     * - Long-lived: 7 ngày
     * - Lưu an toàn ở client (httpOnly cookie recommended)
     */
    @JsonProperty("refresh_token")
    private String refreshToken;

    /**
     * Access token expiration time (seconds)
     */
    @JsonProperty("expires_in")
    private long expiresIn;

    /**
     * Refresh token expiration time (seconds)
     */
    @JsonProperty("refresh_expires_in")
    private long refreshExpiresIn;

    /**
     * Token type: "Bearer"
     */
    @JsonProperty("token_type")
    @Builder.Default
    private String tokenType = "Bearer";

    /**
     * User information (optional)
     */
    private UserInfo user;

    /**
     * Nested class cho user info
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String role;
    }

    // ===================================================================
    // LEGACY SUPPORT - Backward compatibility
    // ===================================================================
    
    /**
     * @deprecated Sử dụng getAccessToken() thay thế
     */
    @Deprecated
    public String getToken() {
        return this.accessToken;
    }

    /**
     * @deprecated Sử dụng setAccessToken() thay thế
     */
    @Deprecated
    public void setToken(String token) {
        this.accessToken = token;
    }
}
