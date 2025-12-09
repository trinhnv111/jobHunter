package trinhnv.jobOKO.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor

public class SkillRequest {

    @NotBlank(message = "Tên kĩ năng là bắt buộc")
    private String skillName;
}
