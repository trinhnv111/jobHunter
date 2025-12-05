package trinhnv.jobOKO.domain.mapper;

import java.util.List;

public interface BaseMapper<D, E> {

    // chuyển đổi enties sang dto
    D toDto(E entity);

    // chuyển dto sang entites
    E toEntity(D dto);

    List<D> toDtoList(List<E> entityList);

    List<E> toEntityList(List<D> dtoList);
}
