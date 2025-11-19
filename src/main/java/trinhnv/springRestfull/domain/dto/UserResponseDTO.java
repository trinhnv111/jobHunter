package trinhnv.springRestfull.domain.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import trinhnv.springRestfull.util.constant.Gender;

@Getter
@Setter
@Data

public class UserResponseDTO {
    private Long id;

    private String userName;

    private String email;

    private String password;

    private int age;

    private Gender gender;

    private String address;

    private String refreshToken;


}

