package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;
import trinhnv.jobOKO.domain.request.CompanyRequest;
import trinhnv.jobOKO.domain.response.CompanyResponse;
import trinhnv.jobOKO.domain.entity.Company;

import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    unmappedTargetPolicy = ReportingPolicy.IGNORE
)
public interface CompanyMapper {

    Company toEntity(CompanyRequest request);

    @Mapping(target = "companyId", ignore = true)
    void updateEntityFromRequest(CompanyRequest request, @MappingTarget Company company);
    
    // Response mapping
    CompanyResponse toResponse(Company entity);
    
   List<CompanyResponse> toResponseList(java.util.List<Company> entityList);
}
