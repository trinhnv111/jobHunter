package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import trinhnv.jobOKO.domain.entity.Skill;
import trinhnv.jobOKO.domain.request.SkillRequest;
import trinhnv.jobOKO.domain.response.SkillResponse;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SkillMapper extends BaseMapper<SkillRequest, Skill> {
    SkillResponse toResponse(Skill skill);
}
