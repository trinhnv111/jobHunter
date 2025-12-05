package trinhnv.jobOKO.service.impl;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import trinhnv.jobOKO.domain.entity.Company;
import trinhnv.jobOKO.domain.mapper.CompanyMapper;
import trinhnv.jobOKO.domain.request.CompanyDTO;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.repository.CompanyRespository;
import trinhnv.jobOKO.service.CompanyService;

@Service
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRespository companyRespository;
    private final CompanyMapper companyMapper;

    public CompanyServiceImpl(CompanyRespository companyRespository, CompanyMapper companyMapper) {
        this.companyRespository = companyRespository;
        this.companyMapper = companyMapper;
    }

    @Override
    public ResultPaginationResponse<CompanyDTO> getAllCompany(Pageable pageable, Specification<Company> spec) {
        Page<Company> companyPage = (spec != null) ? this.companyRespository.findAll(spec, pageable) : this.companyRespository.findAll(pageable);
        return ResultPaginationResponse.ok(companyPage, companyMapper::toDto);
    }

    @Override
    public CompanyDTO getCompanyById(Long id) {
        Company company = this.companyRespository.findById(id).orElseThrow(() -> new BadCredentialsException("Không tìm thấy công ty"));
        return this.companyMapper.toDto(company);
    }

    @Override
    public CompanyDTO handleCreateCompany(CompanyDTO companyDTO) {
        if (this.companyRespository.findByName(companyDTO.getName()) != null) {
            throw new BadCredentialsException("Công ty đã tồn tại");
        }
        Company company = this.companyMapper.toEntity(companyDTO);
        return this.companyMapper.toDto(this.companyRespository.save(company));
    }

    @Override
    public CompanyDTO handleUpdateCompany(Long id, CompanyDTO companyDTO) {
        Company company = this.companyRespository.findById(id).orElseThrow(() -> new BadCredentialsException("không tìm thấy công ty"));
        this.companyMapper.updateEntityFromDto(companyDTO, company);
        return companyMapper.toDto(this.companyRespository.save(company));
    }

    @Override
    public void deleteCompany(Long id) {
        this.companyRespository.findById(id).orElseThrow(() -> new BadCredentialsException(" Không tìm thấy công ty"));
        this.companyRespository.deleteById(id);
    }
}

