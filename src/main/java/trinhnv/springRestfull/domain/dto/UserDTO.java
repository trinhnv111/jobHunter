package trinhnv.springRestfull.domain.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Data
public class UserDTO {
    private  Long id;
    private String userName;
    private String passWord;
}
