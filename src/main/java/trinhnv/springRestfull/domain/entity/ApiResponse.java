package trinhnv.springRestfull.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ApiResponse<T> {
    private int statusCode;  // 0 = success, 1 = failure
    private String error;
    private Object message;
    private T data;


    public static <T> ApiResponse<T> success(Object message, T data) {
        return ApiResponse.<T>builder()
                .statusCode(0)
                .message(message)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> failed(String error, Object message) {
        return ApiResponse.<T>builder()
                .statusCode(1)
                .error(error)
                .message(message)
                .data(null)
                .build();
    }
}
