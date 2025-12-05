package trinhnv.jobOKO.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import trinhnv.jobOKO.domain.entity.Company;
import trinhnv.jobOKO.domain.request.CompanyDTO;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;

public interface CompanyService {
    ResultPaginationResponse<CompanyDTO> getAllCompany(Pageable pageable, Specification<Company> spec);

    CompanyDTO getCompanyById(Long id);

    CompanyDTO handleCreateCompany(CompanyDTO companyDTO);

    CompanyDTO handleUpdateCompany(Long id, CompanyDTO companyDTO);

    void deleteCompany(Long id);
}
