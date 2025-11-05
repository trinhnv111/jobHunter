package trinhnv.springRestfull.service.error;


import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import trinhnv.springRestfull.domain.ApiResponse;

@ControllerAdvice
@RequestMapping
public class GlobalException{


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

}
