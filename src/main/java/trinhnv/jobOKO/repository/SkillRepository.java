package trinhnv.jobOKO.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trinhnv.jobOKO.domain.entity.Skill;

import java.util.List;

public interface SkillRepository extends JpaRepository<Skill, Long> , JpaSpecificationExecutor<Skill> {

    @Query("SELECT s.skillId FROM skill s WHERE s.skillId IN :skillIds")
    List<Long> findSkillIdsByIdIn(@Param("skillIds") List<Long> skillIds);
}
