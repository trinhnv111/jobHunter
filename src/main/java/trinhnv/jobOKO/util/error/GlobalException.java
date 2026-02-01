package trinhnv.jobOKO.util.error;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import trinhnv.jobOKO.domain.entity.ApiResponse;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * ===================================================================
 * GLOBAL EXCEPTION HANDLER (STATELESS)
 * ===================================================================
 * 
 * Xử lý exceptions cho ứng dụng stateless JWT.
 * 
 * @author trinhnv
 */
@ControllerAdvice
@RequestMapping
public class GlobalException {

    private static final Logger log = LoggerFactory.getLogger(GlobalException.class);

    // ===================================================================
    // AUTHENTICATION EXCEPTIONS (LOGIN)
    // ===================================================================

    /**
     * Sai mật khẩu khi đăng nhập (user tồn tại nhưng password không khớp).
     * Trả 401 + thông báo rõ ràng cho client.
     */
    @ExceptionHandler(BadCredentialsException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        log.warn("Login failed: {}", ex.getMessage());

        ApiResponse<Object> res = new ApiResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("INVALID_CREDENTIALS");
        res.setMessage("Tên đăng nhập hoặc mật khẩu không đúng. Vui lòng thử lại.");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    /**
     * Tài khoản không tồn tại (username không tìm thấy trong DB).
     */
    @ExceptionHandler(UsernameNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleUsernameNotFound(UsernameNotFoundException ex) {
        log.warn("Login failed - user not found: {}", ex.getMessage());

        ApiResponse<Object> res = new ApiResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("USER_NOT_FOUND");
        res.setMessage("Tài khoản không tồn tại. Vui lòng kiểm tra lại tên đăng nhập.");
        res.setData(null);

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    @ExceptionHandler(UserPrincipalNotFoundException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleUserPrincipalNotFound(UserPrincipalNotFoundException ex) {
        ApiResponse<Object> res = new ApiResponse<>();
        res.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        res.setError("USER_NOT_FOUND");
        res.setMessage(ex.getMessage() != null ? ex.getMessage() : "Tài khoản không tồn tại.");
        res.setData(null);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(res);
    }

    // ===================================================================
    // JWT EXCEPTIONS (STATELESS)
    // ===================================================================

    /**
     * Xử lý JWT Exception (token hết hạn, invalid signature, etc.)
     */
    @ExceptionHandler(JwtException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleJwtException(JwtException ex) {
        log.warn("JWT Exception: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        response.setError("INVALID_TOKEN");
        response.setMessage("Token không hợp lệ hoặc đã hết hạn");
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    /**
     * Xử lý InvalidTokenException
     */
    @ExceptionHandler(InvalidTokenException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleInvalidTokenException(InvalidTokenException ex) {
        log.warn("Invalid token: {}", ex.getMessage());
        
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatusCode(HttpStatus.UNAUTHORIZED.value());
        response.setError("INVALID_TOKEN");
        response.setMessage(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
    }

    // ===================================================================
    // VALIDATION EXCEPTIONS
    // ===================================================================

    /**
     * ID sai format
     */
    @ExceptionHandler({IdInvalidException.class, MethodArgumentTypeMismatchException.class})
    @ResponseBody
    private ResponseEntity<ApiResponse<Object>>handleIdInvalidException(IdInvalidException ex, WebRequest webRequest) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiResponse.setError(ex.getMessage());
        apiResponse.setMessage("CALL API FAILED");


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

    }

    /**
     * Validation errors
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        List<Map<String, Object>> errorDetails = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> {
                    Map<String, Object> errorMap = new HashMap<>();
                    errorMap.put("field", error.getField());
                    errorMap.put("message", error.getDefaultMessage());
                    return errorMap;
                })
                .collect(Collectors.toList());

        ApiResponse<Object> response = new ApiResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "lỗi cú pháp hoặc dữ liệu sai định dạng",
                errorDetails,
                null
        );

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    // ===================================================================
    // GENERAL EXCEPTIONS
    // ===================================================================

    /**
     * Xử lý RuntimeException chung
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleRuntimeException(RuntimeException ex) {
        log.error("Runtime exception: ", ex);
        
        ApiResponse<Object> response = new ApiResponse<>();
        response.setStatusCode(HttpStatus.BAD_REQUEST.value());
        response.setError("RUNTIME_ERROR");
        response.setMessage(ex.getMessage());
        
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}
