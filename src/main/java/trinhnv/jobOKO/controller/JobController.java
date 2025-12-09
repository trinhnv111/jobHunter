package trinhnv.jobOKO.controller;

import com.turkraft.springfilter.boot.Filter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import trinhnv.jobOKO.domain.entity.Job;
import trinhnv.jobOKO.domain.projection.JobsDetailProjections;
import trinhnv.jobOKO.domain.request.JobRequest;
import trinhnv.jobOKO.domain.response.JobResponse;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.repository.JobResponsitory;
import trinhnv.jobOKO.service.JobService;

@RestController
@RequiredArgsConstructor
public class JobController {
    private final JobService jobService;
    private final JobResponsitory  jobResponsitory;
    
    @GetMapping("/job")
    public ResponseEntity<ResultPaginationResponse<JobResponse>> getAllJobs(@Filter  Specification<Job> specification, Pageable pageable){
        ResultPaginationResponse<JobResponse> jobRespons = this.jobService.getAllJobs(specification,pageable);
        return ResponseEntity.ok().body(jobRespons);
    }


    @GetMapping("/job/{jobId}")
    public ResponseEntity<JobsDetailProjections> getJobId(@PathVariable Long jobId) {
        return ResponseEntity.ok().body(this.jobService.getJobSkillId(jobId));
    }

    @PostMapping("/job")
    public ResponseEntity<JobResponse> createJobs(@RequestBody JobRequest jobRequest)
    {
        return ResponseEntity.ok().body(this.jobService.createJobs(jobRequest));
    }

    @PutMapping("/job/{jobId}")
    public ResponseEntity<JobResponse> updateJobId(@PathVariable Long jobId, @RequestBody JobRequest jobRequest)
    {
        return ResponseEntity.ok().body(this.jobService.updateJobSkillId(jobId,jobRequest));
    }

    @DeleteMapping("/job/{jobId}")
    public ResponseEntity<String> deleteJobId(@PathVariable Long jobId)
    {
        this.jobService.deleteJobSkillId(jobId);
        return ResponseEntity.ok().body("");
    }

}
