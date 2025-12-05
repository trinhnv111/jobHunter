package trinhnv.jobOKO.domain.request;

import lombok.*;
import trinhnv.jobOKO.domain.response.ResLoginDTO;

/**
 * Internal DTO để truyền kết quả login giữa Service và Controller.
 * 
 * Controller sẽ:
 * - Set refreshToken vào httpOnly cookie
 * - Build ResLoginDTO (không chứa refreshToken) để trả về client
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResult {
    
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private long refreshExpiresIn;
    private ResLoginDTO.UserInfo user;
    
    /**
     * Convert to ResLoginDTO (không chứa refreshToken)
     * RefreshToken sẽ được set vào cookie riêng
     */
    public ResLoginDTO toResLoginDTO() {
        return ResLoginDTO.builder()
                .accessToken(this.accessToken)
                .expiresIn(this.expiresIn)
                .tokenType("Bearer")
                .user(this.user)
                .build();
    }
}

