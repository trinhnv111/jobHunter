package trinhnv.jobOKO.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import trinhnv.jobOKO.domain.entity.Job;
import trinhnv.jobOKO.domain.entity.JobSkill;
import trinhnv.jobOKO.domain.entity.Skill;
import trinhnv.jobOKO.domain.mapper.JobMapper;
import trinhnv.jobOKO.domain.projection.JobsDetailProjections;
import trinhnv.jobOKO.domain.request.JobRequest;
import trinhnv.jobOKO.domain.response.JobResponse;
import trinhnv.jobOKO.domain.response.ResultPaginationResponse;
import trinhnv.jobOKO.repository.CompanyRespository;
import trinhnv.jobOKO.repository.JobResponsitory;
import trinhnv.jobOKO.repository.JobSkillRepository;
import trinhnv.jobOKO.repository.SkillRepository;
import trinhnv.jobOKO.service.JobService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {

    private final CompanyRespository companyRespository;
    private final JobResponsitory jobResponsitory;
    private final JobSkillRepository jobSkillRepository;
    private final SkillRepository skillRepository;
    private final JobMapper jobMapper;

    @Override
    public ResultPaginationResponse<JobResponse> getAllJobs(Specification<Job> specification, Pageable pageable) {
        Page<Job> jobsPage = (specification != null) ? this.jobResponsitory.findAll(specification, pageable) : this.jobResponsitory.findAll(pageable);
        return ResultPaginationResponse.ok(jobsPage, jobMapper::toResponse);
    }

    @Override
    public JobsDetailProjections getJobSkillId(Long skillId) {
        JobsDetailProjections job = jobResponsitory.getJobDetail(skillId);

        if (job == null) {
            throw new BadCredentialsException("CÔNG VIỆC KHÔNG TỒN TẠI");
        }

        return job;
    }

    @Override
    @Transactional
    public JobResponse createJobs(JobRequest jobRequest) {
        // 1. Fix logic: Nếu CÓ skillsId thì mới validate (không phải null/empty)
        if (jobRequest.getSkillsId() != null && !jobRequest.getSkillsId().isEmpty()) {
            List<Long> existingSkillIds = this.skillRepository.findSkillIdsByIdIn(jobRequest.getSkillsId());

            // Lấy ra id không tồn tại
            List<Long> missingSkillIds = jobRequest.getSkillsId().stream()
                    .filter(skillId -> !existingSkillIds.contains(skillId))
                    .collect(Collectors.toList());

            if (!missingSkillIds.isEmpty()) {
                throw new BadCredentialsException(
                        "Các skill ID không tồn tại: " + missingSkillIds
                );
            }
        }

        // 2. Validate company
        if (!companyRespository.existsById(jobRequest.getCompanyId())) {
            throw new BadCredentialsException("Công ty không tồn tại");
        }

        // 3. Tạo và lưu Job
        Job job = this.jobMapper.toEntity(jobRequest);
        job.setCompanyId(jobRequest.getCompanyId());
        Job savedJob = jobResponsitory.save(job);

        // 4. Tạo JobSkill records (PHẦN BỔ SUNG)
        if (jobRequest.getSkillsId() != null && !jobRequest.getSkillsId().isEmpty()) {
            List<JobSkill> jobSkills = jobRequest.getSkillsId().stream()
                    .map(skillId -> {
                        JobSkill jobSkill = new JobSkill();
                        jobSkill.setJobId(savedJob.getJobId());
                        jobSkill.setSkillId(skillId);
                        return jobSkill;
                    })
                    .collect(Collectors.toList());

            jobSkillRepository.saveAll(jobSkills);
        }

        // 5. Tạo response và set skillsId
        JobResponse response = this.jobMapper.toResponse(savedJob);
        response.setSkillsId(jobRequest.getSkillsId());

        return response;
    }


    @Override
    public JobResponse updateJobSkillId(Long jobId, JobRequest jobRequest) {
        Job job = this.jobResponsitory.findById(jobId).orElseThrow(() -> {
            return new BadCredentialsException("Công việc không tồn tại");
        });

        this.jobMapper.updateEntityFromDto(jobRequest, job);

        return this.jobMapper.toResponse(jobResponsitory.save(job));
    }

    @Override
    public void deleteJobSkillId(Long jobId) {
        Job job = this.jobResponsitory.findById(jobId).orElseThrow(() -> {
            return new BadCredentialsException("Công việc không tồn tại");
        });
        this.jobResponsitory.deleteById(jobId);
    }
}
