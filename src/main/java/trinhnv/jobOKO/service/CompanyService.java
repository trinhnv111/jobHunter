package trinhnv.jobOKO.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import trinhnv.jobOKO.domain.entity.Company;
import trinhnv.jobOKO.domain.request.CompanyRequest;
import trinhnv.jobOKO.domain.response.CompanyResponse;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;

public interface CompanyService {
    ResultPaginationResponse<CompanyResponse> getAllCompany(Pageable pageable, Specification<Company> spec);

    CompanyResponse getCompanyById(Long id);

    List<CompanyResponse> handleCreateCompanies(List<CompanyRequest> requests);

    CompanyResponse handleUpdateCompany(Long id, CompanyRequest request);

    void deleteCompany(Long id);
}
