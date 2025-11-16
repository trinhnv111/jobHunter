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
}
