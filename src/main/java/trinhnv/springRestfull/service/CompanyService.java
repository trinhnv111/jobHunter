package trinhnv.springRestfull.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import trinhnv.springRestfull.domain.dto.CompanyDTO;
import trinhnv.springRestfull.domain.entity.Company;
import trinhnv.springRestfull.domain.mapper.CompanyMapper;
import trinhnv.springRestfull.repository.CompanyRespository;
import trinhnv.springRestfull.util.response.ResultPaginationResponse;

import java.util.List;

@Service
public class CompanyService {
    private final CompanyRespository companyRespository;
    private final CompanyMapper companyMapper;
    
    public CompanyService(CompanyRespository companyRespository, CompanyMapper companyMapper) {
        this.companyRespository = companyRespository;
        this.companyMapper = companyMapper;
    }


    public ResultPaginationResponse<CompanyDTO> getAllCompany(Pageable pageable, Specification<Company> spec) {
        Page<Company> companyPage = (spec != null) ? this.companyRespository.findAll(spec, pageable) : this.companyRespository.findAll(pageable);
        
        // Convert Page<Company> sang ResultPaginationResponse<CompanyDTO>
        return ResultPaginationResponse.ok(companyPage, companyMapper::toDto);
    }

    public CompanyDTO getCompanyById(Long id) {
        Company company = this.companyRespository.findById(id).orElseThrow(()-> new BadCredentialsException("Không tìm thấy công ty"));
        return this.companyMapper.toDto(company);
    }

    public CompanyDTO handleCreateCompany(CompanyDTO companyDTO) {
        // kiểm tra trùng tên
        if(this.companyRespository.findByName(companyDTO.getName()) != null){
            throw new BadCredentialsException("Công ty đã tồn tại");
        }
        Company company = this.companyMapper.toEntity(companyDTO);
        return this.companyMapper.toDto(this.companyRespository.save(company));
    }

    public CompanyDTO handleUpdateCompany(Long id,CompanyDTO companyDTO) {
         // lấy entites từ db -> map dto sang entites -> lưu lại -> trả lại ra dto
        Company company =  this.companyRespository.findById(id).orElseThrow(()-> new BadCredentialsException("không tìm thấy công ty"));
       this.companyMapper.updateEntityFromDto(companyDTO,company);
        return companyMapper.toDto( this.companyRespository.save(company));

    }
}
