package trinhnv.jobOKO.domain.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Data

public class BaseDTO {
    private String createdBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant createdDate = Instant.now();

    @Column(name = "updated_by")
    private String updatedBy;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss a", timezone = "GMT+7")
    private Instant updatedAt;
}
