package trinhnv.jobOKO.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import trinhnv.jobOKO.util.constant.Gender;

@Data
@EqualsAndHashCode(callSuper=false)

public class UserDTO extends BaseDTO {
    private Long userId;

    @NotBlank(message = "tên tài khoản là bắt buộc")
    private String userName;

    @NotBlank(message = "email khoản là bắt buộc")
    private String email;

    @NotBlank(message = "mật khẩu khoản là bắt buộc")
    private String passWord;

    private Integer age;

    private Gender gender;

    private String address;

    private Long companyId;
}
