package trinhnv.jobOKO.service;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import trinhnv.jobOKO.domain.entity.Skill;
import trinhnv.jobOKO.domain.request.SkillRequest;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.domain.response.SkillResponse;

public interface SkillService {

     ResultPaginationResponse<SkillResponse>getAllSkill(Specification<Skill> spec, Pageable pageable) ;


     SkillResponse getSkillById(@PathVariable("skillId") Long skillId);


     SkillRequest addSkill(@RequestBody SkillRequest skillRequest) ;


     SkillRequest updateSkill(@PathVariable("skillId") Long skillId, SkillRequest skillRequest) ;


     void deleteSkill(@PathVariable("skillId") Long skillId);
}
