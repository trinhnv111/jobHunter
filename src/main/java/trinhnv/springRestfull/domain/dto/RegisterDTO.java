package trinhnv.springRestfull.domain.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import trinhnv.springRestfull.util.constant.Gender;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter

public class RegisterDTO extends BaseDTO {

    @NotBlank(message="tài khoản không được để trống")
    @NotNull
    private String username;

    @NotBlank(message="mật khẩu không được để trống")
    @NotNull
    private String password;

    @NotBlank(message="email không được để trống")
    @NotNull
    private String email;

    @Min(value = 18,message = "Tuổi phải lớn hơn 18")
    private int age;

    private Gender gender;

    private String address;

}
