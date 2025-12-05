package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.*;
import trinhnv.jobOKO.domain.request.RegisterDTO;
import trinhnv.jobOKO.domain.request.UserDTO;
import trinhnv.jobOKO.domain.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper extends BaseMapper<UserDTO, User> {


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromDTO(UserDTO userDTO, @MappingTarget User updatedUser);

    @Mapping(target = "passWord", ignore = true)
    User registerUser(RegisterDTO registerDTO);

}
