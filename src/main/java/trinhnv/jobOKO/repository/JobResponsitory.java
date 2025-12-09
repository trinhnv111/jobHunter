package trinhnv.jobOKO.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import trinhnv.jobOKO.domain.entity.Job;
import trinhnv.jobOKO.domain.projection.JobsDetailProjections;

public interface JobResponsitory extends JpaRepository<Job, Long> , JpaSpecificationExecutor<Job> {

    @Query("""
        SELECT 
            j.jobId AS jobId,
            j.name AS jobName,
            j.description AS jobDescription,
            j.salary AS salary,
            j.location AS location,
            j.quantity AS quantity,
            j.level AS level,
            j.isActive AS isActive,
            j.startDate AS startDate,
            j.endDate AS endDate,

            c.companyId AS companyId,
            c.name AS companyName,
            c.description AS companyDescription,
            c.address AS companyAddress,
            c.logo AS companyLogo

        FROM jobs j
        JOIN companies c on c.companyId = j.companyId
        WHERE j.jobId = :jobId
        """)
    JobsDetailProjections getJobDetail(@Param("jobId") Long jobId);
}
