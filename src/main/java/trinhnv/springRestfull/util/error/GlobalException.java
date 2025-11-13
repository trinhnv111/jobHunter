package trinhnv.springRestfull.util.error;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import trinhnv.springRestfull.domain.ApiResponse;

import java.nio.file.attribute.UserPrincipalNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
@RequestMapping
public class GlobalException{

    // xử lý exception login user/password sai -->>>401
    @ExceptionHandler(value = {
            UserPrincipalNotFoundException.class,
            BadCredentialsException.class
    })
    @ResponseBody
    public ResponseEntity<ApiResponse<Object>> handleUserPrincipalNotFound(Exception ex){
        ApiResponse<Object> res = new ApiResponse<>();
        res.setStatusCode(HttpStatus.BAD_REQUEST.value());
        res.setError(ex.getMessage());
        res.setMessage("Bad Credentials");

        return  ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }



    // id sai format
    @ExceptionHandler({IdInvalidException.class, MethodArgumentTypeMismatchException.class})
    @ResponseBody
    private ResponseEntity<ApiResponse<Object>>handleIdInvalidException(IdInvalidException ex, WebRequest webRequest) {

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setStatusCode(HttpStatus.BAD_REQUEST.value());
        apiResponse.setError(ex.getMessage());
        apiResponse.setMessage("CALL API FAILED");


        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiResponse);

    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException ex) {

        // Tạo danh sách lỗi chi tiết
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

}
