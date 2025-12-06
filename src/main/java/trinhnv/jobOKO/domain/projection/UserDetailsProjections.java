package trinhnv.jobOKO.domain.projection;

public interface UserDetailsProjections {
    Long getUserId();
    String getUserName();
    String getEmail();
    String getAddress();
    Integer getAge();
    String getGender();
    
    // Company fields
    Long getCompanyId();
    String getCompanyName();
    String getCompanyLogo();
    String getCompanyAddress();
    String getCompanyDescription();
}

