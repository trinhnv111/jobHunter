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

        // 4. Case thành công, không có annotation -> dùng default message
        return ApiResponse.builder()
                .statusCode(0)
                .message("SUCCESS")  // default nếu không có @ApiMessage
                .data(body)
                .build();
    }
}
