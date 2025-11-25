package trinhnv.springRestfull.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
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
 * - Access Token: JWT, short-lived (trả về trong body)
 * - Refresh Token: JWT, long-lived (set vào httpOnly cookie, không trả về body)
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
@JsonInclude(JsonInclude.Include.NON_NULL)  // Không serialize fields null
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
     * Access token expiration time (seconds)
     */
    @JsonProperty("expires_in")
    private long expiresIn;

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
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserInfo {
        private Long id;
        private String username;
        private String email;
        private String role;
    }
}
