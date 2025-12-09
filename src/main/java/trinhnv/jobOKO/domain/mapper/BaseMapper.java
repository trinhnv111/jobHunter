package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

public interface BaseMapper<D, E> {

    // chuyển đổi enties sang dto
    D toDto(E entity);

    // chuyển dto sang entites
    E toEntity(D dto);

    List<D> toDtoList(List<E> entityList);

    List<E> toEntityList(List<D> dtoList);

    // Copy dữ liệu từ DTO vào Entity đã có sẵn
    // nullValuePropertyMappingStrategy = IGNORE giúp giữ nguyên giá trị cũ nếu DTO truyền vào null
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(D dto, @MappingTarget E entity);
}
