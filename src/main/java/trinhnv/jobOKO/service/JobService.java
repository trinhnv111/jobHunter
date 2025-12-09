package trinhnv.jobOKO.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.domain.entity.Job;
import trinhnv.jobOKO.domain.projection.JobsDetailProjections;
import trinhnv.jobOKO.domain.request.JobRequest;
import trinhnv.jobOKO.domain.response.JobResponse;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;

public interface JobService {
   
     ResultPaginationResponse<JobResponse>getAllJobs(Specification<Job>  specification, Pageable pageable);

    JobsDetailProjections getJobSkillId(Long skillId);

     JobResponse createJobs(JobRequest jobRequest);

     JobResponse updateJobSkillId(Long jobId, JobRequest jobRequest);

     void deleteJobSkillId(Long jobId);

}
