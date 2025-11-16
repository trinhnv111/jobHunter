package trinhnv.springRestfull.util;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import trinhnv.springRestfull.common.annotation.ApiMessage;
import trinhnv.springRestfull.domain.entity.ApiResponse;

@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType,
                            Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
                                  MethodParameter returnType,
                                  MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  ServerHttpRequest request,
                                  ServerHttpResponse response) {

        HttpServletResponse httpServletResponse =
                ((ServletServerHttpResponse) response).getServletResponse();

        int status = httpServletResponse.getStatus();

        // 1. Nếu controller OR AOP đã trả ApiResponse -> không wrap lại
        if (body instanceof ApiResponse) {
            return body;
        }

        // 2. Nếu trả về String -> không xử lý (Spring không cho wrap String)
        if (body instanceof String) {
            return body;
        }

        // 3. Nếu status lỗi -> để GlobalException xử lý -> không wrap
        if (status >= 400) {
            return body;
        }

        // 4. Đọc trực tiếp annotation @ApiMessage từ method (KHÔNG CẦN RequestAttributes)
        String message = getMessageFromAnnotation(returnType);
        
        // 5. Case thành công
        return ApiResponse.builder()
                .statusCode(0)
                .message(message != null ? message : "CALL API SUCCEEDED")  // Dùng message từ @ApiMessage hoặc default
                .data(body)
                .build();
    }

    /**
     * Đọc trực tiếp annotation @ApiMessage từ MethodParameter
     * Đơn giản hơn, không cần AOP và RequestAttributes
     * 
     * @param returnType MethodParameter chứa thông tin về method được gọi
     * @return Message từ @ApiMessage annotation hoặc null nếu không có
     */
    private String getMessageFromAnnotation(MethodParameter returnType) {
        // Đọc annotation @ApiMessage từ method
        ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
        if (apiMessage != null) {
            return apiMessage.value();
        }
        return null;
    }
}
