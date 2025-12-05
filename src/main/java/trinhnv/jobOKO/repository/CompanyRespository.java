package trinhnv.jobOKO.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import trinhnv.jobOKO.domain.entity.Company;

public interface CompanyRespository extends JpaRepository<Company, Long>,
        JpaSpecificationExecutor<Company> {
    Company findByName(String name);

}
