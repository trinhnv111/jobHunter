package trinhnv.springRestfull.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import trinhnv.springRestfull.util.constant.Gender;

@Getter
@Setter
@Data
@Entity
@Table(name="user")
public class User extends AbstractAuditingEntity <Long>{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    private String userName;

    private String email;

    private String passWord;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;

}
