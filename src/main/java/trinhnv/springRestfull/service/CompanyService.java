package trinhnv.springRestfull.service;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import trinhnv.springRestfull.domain.dto.CompanyDTO;
import trinhnv.springRestfull.domain.entity.Company;
import trinhnv.springRestfull.domain.mapper.CompanyMapper;
import trinhnv.springRestfull.repository.CompanyRespository;

import java.util.List;
@Service

public class CompanyService {
    private final CompanyRespository companyRespository;
    private final CompanyMapper companyMapper;
    public CompanyService(CompanyRespository companyRespository,CompanyMapper companyMapper) {
        this.companyRespository = companyRespository;
        this.companyMapper = companyMapper;
    }

    public List<CompanyDTO> getAllCompany() {
        List<Company> company = this.companyRespository.findAll();
        return companyMapper.toDtoList(company);
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
