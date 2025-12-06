package trinhnv.jobOKO.domain.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyResponse {
    private Long companyId;
    private String name;
    private String description;
    private String address;
    private String logo;
}

