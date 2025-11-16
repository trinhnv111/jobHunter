package trinhnv.springRestfull.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@Data

public class LoginDTO {
    
    @NotBlank(message = "tên đăng nhập không được bỏ trống")
    private String username;

    @NotBlank(message = "mật khẩu không được bỏ trống")
    private String password;


}
