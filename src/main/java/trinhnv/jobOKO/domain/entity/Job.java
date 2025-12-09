package trinhnv.jobOKO.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import trinhnv.jobOKO.util.constant.Level;

import java.time.LocalDateTime;

@Table
@Entity(name = "jobs")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Job extends AbstractAuditingEntity<Long> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    private Long companyId;

    private String name;
    private String location;
    private Double salary;
    private Integer quantity;
    private Level level;

    @Column(columnDefinition = "MEDIUMTEXT")
    private String description;

    LocalDateTime startDate;
    LocalDateTime endDate;
    Boolean isActive;

}
