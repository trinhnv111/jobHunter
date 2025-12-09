package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import trinhnv.jobOKO.domain.request.CompanyRequest;
import trinhnv.jobOKO.domain.response.CompanyResponse;
import trinhnv.jobOKO.domain.entity.Company;

import java.util.List;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper {

    // Request mapping
    Company toEntity(CompanyRequest request);
    
    // nếu dùng toEntity thì sẽ tạo ra 1 đối tượng mới ->> kh sửa đc đối tượng ban đầu
    void updateEntityFromRequest(CompanyRequest request, @MappingTarget Company company);
    
    // Response mapping
    CompanyResponse toResponse(Company entity);
    
   List<CompanyResponse> toResponseList(java.util.List<Company> entityList);
}
