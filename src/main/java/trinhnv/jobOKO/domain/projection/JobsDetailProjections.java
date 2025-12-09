package trinhnv.jobOKO.domain.projection;

import java.time.LocalDateTime;

public interface JobsDetailProjections {
    Long getJobId();
    String getJobName();
    String getJobDescription();
    Double getSalary();
    String getLocation();
    Integer getQuantity();
    String getLevel();
    Boolean getIsActive();
    LocalDateTime getStartDate();
    LocalDateTime getEndDate();

    Long getCompanyId();
    String getCompanyName();
    String getCompanyDescription();
    String getCompanyAddress();
    String getCompanyLogo();

}
