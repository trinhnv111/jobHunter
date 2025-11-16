package trinhnv.springRestfull.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import trinhnv.springRestfull.domain.entity.Company;

import java.util.List;

public interface CompanyRespository extends JpaRepository<Company, Long> {
    Company findByName(String name);

    List<Company> id(Long id);
}
