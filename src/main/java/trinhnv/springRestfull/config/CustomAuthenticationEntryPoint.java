package trinhnv.springRestfull.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import trinhnv.springRestfull.domain.ApiResponse;

import java.io.IOException;

/**
 * ===================================================================
 * CUSTOM AUTHENTICATION ENTRY POINT - XỬ LÝ LỖI 401 (UNAUTHORIZED) ⭐
 * ===================================================================
 * 
 * ⭐ ĐÂY LÀ PHẦN CUSTOM ĐỂ XỬ LÝ EXCEPTION KHI TOKEN KHÔNG HỢP LỆ!
 * 
 * Class này được gọi khi:
 * 1. Client gửi request KHÔNG có token
 * 2. Token không hợp lệ (signature sai, format sai)
 * 3. Token đã hết hạn (expired)
 * 4. Token không đúng format
 * 
 * LUỒNG XỬ LÝ:
 * 
 * Request đến với token không hợp lệ
 *   → Spring Security filter chain kiểm tra token
 *   → JwtDecoder.decode() throw exception
 *   → Spring Security catch exception
 *   → Gọi CustomAuthenticationEntryPoint.commence()
 *   → Trả về response 401 với message lỗi
 * 
 * CÁC TRƯỜNG HỢP LỖI:
 * 
 * 1. Không có token:
 *    - Client không gửi header Authorization
 *    - Exception: MissingBearerTokenException
 * 
 * 2. Token không hợp lệ:
 *    - Token bị sửa đổi, signature sai
 *    - Exception: JwtException
 * 
 * 3. Token hết hạn:
 *    - Token đã quá thời gian expiresAt
 *    - Exception: ExpiredJwtException
 * 
 * 4. Token format sai:
 *    - Token không đúng cấu trúc JWT
 *    - Exception: JwtException
 * 
 * CẤU HÌNH:
 * - Class này được đăng ký trong SecurityConfiguration:
 *   .oauth2ResourceServer((oauth2) -> oauth2
 *       .authenticationEntryPoint(customAuthenticationEntryPoint)
 *   )
 * 
 * @author trinhnv
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    /**
     * BearerTokenAuthenticationEntryPoint: Default handler của Spring Security
     * Dùng để set HTTP status code 401 và WWW-Authenticate header
     */
    private final AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

    /**
     * ObjectMapper: Dùng để convert object thành JSON
     * Để trả về response dạng JSON cho client
     */
    private final ObjectMapper objectMapper;

    /**
     * Constructor injection - Spring tự động inject ObjectMapper
     * 
     * @param objectMapper ObjectMapper để convert object thành JSON
     */
    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * ===================================================================
     * COMMENCE - XỬ LÝ KHI XÁC THỰC THẤT BẠI (401 UNAUTHORIZED)
     * ===================================================================
     * 
     * Method này được Spring Security tự động gọi khi:
     * - Token không tồn tại
     * - Token không hợp lệ
     * - Token hết hạn
     * 
     * LUỒNG XỬ LÝ:
     * 
     * Bước 1: Gọi default handler
     *   - Set HTTP status code 401
     *   - Set WWW-Authenticate header (Bearer)
     * 
     * Bước 2: Set response content type
     *   - application/json;charset=UTF-8
     *   - Hỗ trợ tiếng Việt (UTF-8)
     * 
     * Bước 3: Tạo ApiResponse object
     *   - statusCode: 401 (Unauthorized)
     *   - error: Thông tin lỗi từ exception
     *   - message: Message mô tả lỗi
     * 
     * Bước 4: Convert ApiResponse thành JSON và ghi vào response
     *   - ObjectMapper.writeValue() convert object thành JSON
     *   - Ghi vào response.getWriter()
     * 
     * RESPONSE MẪU:
     * {
     *   "statusCode": 401,
     *   "error": "JWT expired at 2024-01-01T00:00:00Z",
     *   "message": "Token hết hạn,không hợp lệ,.........",
     *   "data": null
     * }
     * 
     * ⚠️ LƯU Ý:
     * - authException.getCause() có thể null → Cần check null
     * - Nên log exception để debug
     * - Có thể customize message theo từng loại exception
     * 
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param authException AuthenticationException chứa thông tin lỗi
     * @throws IOException nếu không thể ghi response
     * @throws ServletException nếu có lỗi servlet
     */
    @Override
    public void commence(
            HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException authException
    ) throws IOException, ServletException {
        
        // ============================================================
        // BƯỚC 1: GỌI DEFAULT HANDLER
        // ============================================================
        // Set HTTP status code 401 và WWW-Authenticate header
        // Header: WWW-Authenticate: Bearer
        this.authenticationEntryPoint.commence(request, response, authException);
        
        // ============================================================
        // BƯỚC 2: SET RESPONSE CONTENT TYPE
        // ============================================================
        // application/json;charset=UTF-8 để hỗ trợ tiếng Việt
        response.setContentType("application/json;charset=UTF-8");

        // ============================================================
        // BƯỚC 3: TẠO API RESPONSE OBJECT
        // ============================================================
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(HttpStatus.UNAUTHORIZED.value());  // 401
        
        // Lấy thông tin lỗi từ exception
        // authException.getCause() có thể null → Cần check null
        if (authException.getCause() != null) {
            apiResponse.setError(authException.getCause().getMessage());
        } else {
            apiResponse.setError(authException.getMessage());
        }
        
        // Message mô tả lỗi (có thể customize theo từng loại exception)
        apiResponse.setMessage("Token hết hạn,không hợp lệ,.........");

        // ============================================================
        // BƯỚC 4: CONVERT VÀ GHI RESPONSE
        // ============================================================
        // Convert ApiResponse thành JSON và ghi vào response
        // Client sẽ nhận được response JSON với status code 401
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
