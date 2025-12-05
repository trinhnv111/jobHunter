package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import trinhnv.jobOKO.domain.request.CompanyDTO;
import trinhnv.jobOKO.domain.entity.Company;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper extends BaseMapper<CompanyDTO, Company> {

    // nếu dùng toenties thì sẽ tạo ra 1 đối tượng mới ->> kh sửa đc đối tượng ban đầu
    void updateEntityFromDto(CompanyDTO companyDTO, @MappingTarget Company company);
}
