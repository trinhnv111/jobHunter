package trinhnv.jobOKO.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import trinhnv.jobOKO.domain.entity.Skill;

public interface SkillRepository extends JpaRepository<Skill, Long> , JpaSpecificationExecutor<Skill> {

}
