package trinhnv.jobOKO.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.domain.entity.Skill;
import trinhnv.jobOKO.service.SkillService;

@RestController
@RequiredArgsConstructor
public class SkillController {
    private final SkillService skillService;

    @GetMapping("/skill")
    public ResponseEntity<String> getAllSkill() {
        return ResponseEntity.ok().body("ok");
    }

    @GetMapping("/skill/{skillId}")
    public ResponseEntity<String> getSkillById(@PathVariable("skillId") String skillId) {
        return ResponseEntity.ok().body("ok");
    }

    @PostMapping("/skill")
    public ResponseEntity<String> addSkill(@RequestBody Skill skill) {
        return ResponseEntity.ok().body("ok");
    }

    @PutMapping("/{skillId}")
    public ResponseEntity<String> updateSkill(@PathVariable("skillId") String skillId, @RequestBody Skill skill) {
        return ResponseEntity.ok().body("ok");
    }

    @DeleteMapping("/{skillId}")
    public ResponseEntity<String> deleteSkill(@PathVariable("skillId") String skillId) {
        return ResponseEntity.ok().body("ok");
    }
}
