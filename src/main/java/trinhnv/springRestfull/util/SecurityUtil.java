package trinhnv.springRestfull.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/**
 * ===================================================================
 * SECURITY UTIL - UTILITY CLASS ĐỂ TẠO JWT TOKEN
 * ===================================================================
 * 
 * Class này chịu trách nhiệm tạo JWT token sau khi xác thực thành công.
 * 
 * LUỒNG SỬ DỤNG:
 * AuthController.login()
 *   → AuthenticationManager.authenticate() thành công
 *   → Gọi SecurityUtil.createToken(authentication)
 *   → Tạo JWT token với thông tin user và thời gian hết hạn
 *   → Trả về token string cho client
 * 
 * CẤU TRÚC JWT TOKEN:
 * Header.Payload.Signature
 * 
 * Header: {
 *   "alg": "HS512",  // Thuật toán mã hóa
 *   "typ": "JWT"
 * }
 * 
 * Payload: {
 *   "sub": "trinhnv",           // Username (subject)
 *   "iat": 1234567890,          // Thời gian tạo (issued at)
 *   "exp": 1234654290,          // Thời gian hết hạn (expires at)
 *   "trinhnv": {...}            // Thông tin authentication (custom claim)
 * }
 * 
 * Signature: HMACSHA512(
 *   base64UrlEncode(header) + "." + base64UrlEncode(payload),
 *   secretKey
 * )
 * 
 * @author trinhnv
 */
@Service
public class SecurityUtil {
    
    /**
     * JwtEncoder: Encoder để tạo và mã hóa JWT token
     * Được inject từ SecurityConfiguration.jwtEncoder()
     */
    private final JwtEncoder jwtEncoder;

    /**
     * Constructor injection - Spring tự động inject JwtEncoder
     * 
     * @param jwtEncoder JwtEncoder từ SecurityConfiguration
     */
    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }
    
    /**
     * ===================================================================
     * JWT ALGORITHM - THUẬT TOÁN MÃ HÓA JWT
     * ===================================================================
     * 
     * HS512 (HMAC-SHA512):
     * - Thuật toán mã hóa đối xứng (symmetric)
     * - Dùng secret key để ký và xác thực token
     * - Nhanh và phù hợp cho ứng dụng single-server
     * 
     * Lưu ý: Nếu có nhiều server, nên dùng RS256 (asymmetric)
     */
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS512;
    
    /**
     * Secret key để ký JWT token (từ application.properties)
     * Lưu ý: Không sử dụng trong class này, chỉ để tham khảo
     */
    @Value("${trinhnguyen.jwtKey}")
    private String jwtSecretKey;
    
    /**
     * Thời gian sống của token (giây) - từ application.properties
     * Ví dụ: 86400 = 24 giờ
     */
    @Value("${trinhnguyen.jwtSecond}")
    private long jwtSecond;

    /**
     * ===================================================================
     * CREATE TOKEN - TẠO JWT TOKEN TỪ AUTHENTICATION OBJECT
     * ===================================================================
     * 
     * Method này được gọi sau khi xác thực thành công để tạo JWT token.
     * 
     * LUỒNG XỬ LÝ:
     * 
     * Bước 1: Tính toán thời gian
     *   - now: Thời điểm hiện tại
     *   - validity: Thời điểm hết hạn (now + jwtSecond giây)
     * 
     * Bước 2: Tạo JwtClaimsSet (Payload của token)
     *   - issuedAt: Thời gian tạo token
     *   - expiresAt: Thời gian hết hạn token
     *   - subject: Username (authentication.getName())
     *   - claim: Thông tin authentication (custom claim)
     * 
     * Bước 3: Tạo JwsHeader
     *   - algorithm: HS512
     * 
     * Bước 4: Mã hóa và ký token
     *   - JwtEncoder.encode() sẽ:
     *     a) Mã hóa header và payload thành Base64URL
     *     b) Ký token bằng secret key (HMAC-SHA512)
     *     c) Tạo chuỗi token: header.payload.signature
     * 
     * Bước 5: Trả về token string
     *   - Client sẽ lưu token này
     *   - Gửi kèm trong header: Authorization: Bearer <token>
     * 
     * ⚠️ LƯU Ý:
     * - Token sẽ hết hạn sau jwtSecond giây
     * - Khi token hết hạn, client phải đăng nhập lại
     * - Token được ký bằng secret key, không thể giả mạo
     * 
     * @param authentication Authentication object sau khi xác thực thành công
     * @return JWT token string (ví dụ: "eyJhbGciOiJIUzUxMiJ9...")
     */
    public String createToken(Authentication authentication) {
        
        // ============================================================
        // BƯỚC 1: TÍNH TOÁN THỜI GIAN
        // ============================================================
        // now: Thời điểm hiện tại
        Instant now = Instant.now();
        
        // validity: Thời điểm hết hạn (now + jwtSecond giây)
        // Ví dụ: jwtSecond = 86400 → Token hết hạn sau 24 giờ
        Instant validity = now.plus(jwtSecond, ChronoUnit.SECONDS);

        // ============================================================
        // BƯỚC 2: TẠO JWT CLAIMS SET (PAYLOAD)
        // ============================================================
        // Claims là thông tin chứa trong token
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)                              // Thời gian tạo token
                .expiresAt(validity)                       // Thời gian hết hạn token
                .subject(authentication.getName())          // Username (subject)
                .claim("trinhnv", authentication)          // Custom claim: Thông tin authentication
                .build();
        
        // ============================================================
        // BƯỚC 3: TẠO JWS HEADER
        // ============================================================
        // Header chứa thuật toán mã hóa (HS512)
        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        
        // ============================================================
        // BƯỚC 4: MÃ HÓA VÀ KÝ TOKEN
        // ============================================================
        // JwtEncoder.encode() sẽ:
        // 1. Mã hóa header và payload thành Base64URL
        // 2. Ký token bằng secret key (HMAC-SHA512)
        // 3. Tạo chuỗi token: header.payload.signature
        // 
        // Ví dụ token:
        // eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ0cmluaG52IiwiZXhwIjoxNzM...
        return this.jwtEncoder.encode(
                JwtEncoderParameters.from(jwsHeader, claims)
        ).getTokenValue();
    }
}
