package trinhnv.jobOKO.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import trinhnv.jobOKO.util.constant.Gender;


@Data
@Entity
@Table(name="user")
public class User extends AbstractAuditingEntity <Long>{

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long userId;

    private String userName;

    private String email;

    private String passWord;

    private int age;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String address;

    private Long companyId;


}
