package trinhnv.jobOKO.domain.response;

import lombok.Data;
import lombok.EqualsAndHashCode;
import trinhnv.jobOKO.domain.request.BaseDTO;
import trinhnv.jobOKO.util.constant.Gender;

@Data
@EqualsAndHashCode(callSuper=false)
public class UserResponse extends BaseDTO {
    private Long userId;
    private String userName;
    private String email;
    private Integer age;
    private Gender gender;
    private String address;
    private Long companyId;
}

