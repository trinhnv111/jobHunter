package trinhnv.jobOKO.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import trinhnv.jobOKO.domain.entity.Skill;
import trinhnv.jobOKO.domain.mapper.SkillMapper;
import trinhnv.jobOKO.domain.request.SkillRequest;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.domain.response.SkillResponse;
import trinhnv.jobOKO.repository.SkillRepository;
import trinhnv.jobOKO.service.SkillService;

@Service
@RequiredArgsConstructor

public class SkillServiceImpl implements SkillService {

    private final SkillRepository skillRepository;
    private final SkillMapper skillMapper;

    @Override
    public ResultPaginationResponse<SkillResponse> getAllSkill(Specification<Skill> spec, Pageable pageable) {
        Page<Skill> skill = (spec != null) ? this.skillRepository.findAll(spec,pageable) :  this.skillRepository.findAll(pageable);

        return ResultPaginationResponse.ok(skill, skillMapper::toResponse);
    }

    @Override
    public SkillResponse getSkillById(Long skillId) {
        Skill skill = this.skillRepository.findById(skillId).orElseThrow(()-> new BadCredentialsException("kỹ năng không tồn tại "));

        return this.skillMapper.toResponse(skill);
    }

    @Override
    @Transactional
    public SkillRequest addSkill(SkillRequest skillRequest) {
        Skill skill = this.skillMapper.toEntity(skillRequest);

        return this.skillMapper.toDto(this.skillRepository.save(skill));
    }

    @Override
    @Transactional
    public SkillRequest updateSkill(Long skillId, SkillRequest skillRequest) {
        Skill skill = this.skillRepository.findById(skillId).orElseThrow(()-> new BadCredentialsException("kỹ năng không tồn tại "));

        this.skillMapper.updateEntityFromDto(skillRequest,skill);

        return this.skillMapper.toDto(this.skillRepository.save(skill));
    }

    @Override
    public void deleteSkill(Long skillId) {
        Skill skill = this.skillRepository.findById(skillId).orElseThrow(()-> new BadCredentialsException("kỹ năng không tồn tại "));
        this.skillRepository.delete(skill);
    }
}
