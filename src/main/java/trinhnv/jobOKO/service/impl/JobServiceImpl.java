package trinhnv.jobOKO.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import trinhnv.jobOKO.domain.entity.Job;
import trinhnv.jobOKO.domain.mapper.JobMapper;
import trinhnv.jobOKO.domain.projection.JobsDetailProjections;
import trinhnv.jobOKO.domain.request.JobRequest;
import trinhnv.jobOKO.domain.response.JobResponse;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.repository.CompanyRespository;
import trinhnv.jobOKO.repository.JobResponsitory;
import trinhnv.jobOKO.service.JobService;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final CompanyRespository  companyRespository;
    private final JobResponsitory jobResponsitory;
    private final JobMapper jobMapper;

    @Override
    public ResultPaginationResponse<JobResponse> getAllJobs(Specification<Job> specification, Pageable pageable) {
        Page<Job> jobsPage = (specification != null) ? this.jobResponsitory.findAll(specification, pageable) : this.jobResponsitory.findAll(pageable);
        return ResultPaginationResponse.ok(jobsPage, jobMapper::toResponse);
    }

    @Override
    public JobsDetailProjections getJobSkillId(Long skillId) {
        JobsDetailProjections job = jobResponsitory.getJobDetail(skillId);

        if(job == null){
            throw  new BadCredentialsException("CÔNG VIỆC KHÔNG TỒN TẠI");
        }

        return job;
    }

    @Override
    public JobResponse createJobs(JobRequest jobRequest) {
      if(!companyRespository.existsById(jobRequest.getCompanyId()))
      {
          throw new BadCredentialsException("Công ty không tồn tại");
      }

      Job job = this.jobMapper.toEntity(jobRequest);
      job.setCompanyId(jobRequest.getCompanyId());


      return this.jobMapper.toResponse(jobResponsitory.save(job));
    }

    @Override
    public JobResponse updateJobSkillId(Long jobId, JobRequest jobRequest) {
        Job job = this.jobResponsitory.findById(jobId).orElseThrow(()->{
            return new BadCredentialsException("Công việc không tồn tại");
        });

        this.jobMapper.updateEntityFromDto(jobRequest,job);

        return this.jobMapper.toResponse(jobResponsitory.save(job));
    }

    @Override
    public void deleteJobSkillId(Long jobId) {
        Job job = this.jobResponsitory.findById(jobId).orElseThrow(()->{
            return new BadCredentialsException("Công việc không tồn tại");
        });
        this.jobResponsitory.deleteById(jobId);
    }
}
