package trinhnv.jobOKO.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Table
@Entity(name = "jobskill")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class JobSkill extends AbstractAuditingEntity<Long>{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobSkillId;

    private Long skillId;

    private Long  jobId;

}
