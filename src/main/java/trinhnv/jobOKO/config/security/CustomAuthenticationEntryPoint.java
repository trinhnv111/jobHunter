package trinhnv.jobOKO.config.security;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import trinhnv.jobOKO.domain.entity.ApiResponse;

import java.io.IOException;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
    private final AuthenticationEntryPoint authenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(
            HttpServletRequest request, 
            HttpServletResponse response, 
            AuthenticationException authException
    ) throws IOException, ServletException {

        this.authenticationEntryPoint.commence(request, response, authException);

        response.setContentType("application/json;charset=UTF-8");


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

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}
