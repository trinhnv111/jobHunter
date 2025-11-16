package trinhnv.springRestfull.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import trinhnv.springRestfull.domain.dto.CompanyDTO;
import trinhnv.springRestfull.domain.entity.Company;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CompanyMapper extends BaseMapper<CompanyDTO, Company> {

    // nếu dùng toenties thì sẽ tạo ra 1 đối tượng mới ->> kh sửa đc đối tượng ban đầu
    void updateEntityFromDto(CompanyDTO companyDTO, @MappingTarget Company company);
}
