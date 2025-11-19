package trinhnv.springRestfull.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import trinhnv.springRestfull.domain.dto.UserDTO;
import trinhnv.springRestfull.domain.entity.User;

@Mapper(componentModel = "spring",
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface UserMapper extends BaseMapper<UserDTO, User> {

    void updateUserFromDTO(UserDTO userDTO, @MappingTarget User updatedUser);

}
