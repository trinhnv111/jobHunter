package trinhnv.jobOKO.domain.response;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import trinhnv.jobOKO.util.constant.Level;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data

public class JobResponse {
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
