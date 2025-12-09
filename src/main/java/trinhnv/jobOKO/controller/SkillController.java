package trinhnv.jobOKO.controller;

import com.turkraft.springfilter.boot.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.domain.entity.Skill;
import trinhnv.jobOKO.domain.request.SkillRequest;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.domain.response.SkillResponse;
import trinhnv.jobOKO.service.SkillService;

@RestController
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping("/skill")
    public ResponseEntity<ResultPaginationResponse<SkillResponse>> getAllSkill(@Filter Specification<Skill> spec, Pageable pageable) {
        return ResponseEntity.ok().body(this.skillService.getAllSkill(spec, pageable));
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<SkillResponse> getSkillById(@PathVariable("skillId") Long skillId) {
        return ResponseEntity.ok().body(this.skillService.getSkillById(skillId));
    }

    @PostMapping("/skill")
    public ResponseEntity<SkillRequest> addSkill(@RequestBody SkillRequest skill) {
        return ResponseEntity.ok().body(this.skillService.addSkill(skill));
    }

    @PutMapping("/skill/{skillId}")
    public ResponseEntity<SkillRequest> updateSkill(@PathVariable("skillId") Long skillId, @RequestBody SkillRequest skill) {
        return ResponseEntity.ok().body(this.skillService.updateSkill(skillId,skill));
    }

    @DeleteMapping("/skill/{skillId}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long skillId) {
        skillService.deleteSkill(skillId);
        return ResponseEntity.noContent().build(); // HTTP 204
    }

}
