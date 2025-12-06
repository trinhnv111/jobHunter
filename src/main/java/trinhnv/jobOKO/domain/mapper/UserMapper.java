package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.*;
import trinhnv.jobOKO.domain.request.RegisterRequest;
import trinhnv.jobOKO.domain.request.UserRequest;
import trinhnv.jobOKO.domain.response.UserResponse;
import trinhnv.jobOKO.domain.entity.User;

import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {
    
    // Request mapping
    User toEntity(UserRequest request);
    
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateUserFromRequest(UserRequest request, @MappingTarget User updatedUser);

    @Mapping(target = "passWord", ignore = true)
    User registerUser(RegisterRequest registerRequest);
    
    // Response mapping
    UserResponse toResponse(User entity);
    
    List<UserResponse> toResponseList(List<User> entityList);
}
