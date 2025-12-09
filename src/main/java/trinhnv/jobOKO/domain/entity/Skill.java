package trinhnv.jobOKO.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table
@Entity(name = "skill")
@NoArgsConstructor
@AllArgsConstructor
@Data

public class Skill extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long skillId;

    private String skillName;

}
