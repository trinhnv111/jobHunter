package trinhnv.jobOKO.domain.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class CompanyDTO {
    private Long companyId;
    @NotBlank(message = "tên công ty không được để trống")
    private String name;
    private String description;
    private String address;
    private String logo;
}
