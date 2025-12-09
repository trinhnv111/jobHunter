package trinhnv.jobOKO.domain.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.NullValuePropertyMappingStrategy;
import trinhnv.jobOKO.domain.entity.Job;
import trinhnv.jobOKO.domain.request.JobRequest;
import trinhnv.jobOKO.domain.response.JobResponse;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface JobMapper extends BaseMapper<JobRequest, Job> {
    JobResponse toResponse(Job job);
}
