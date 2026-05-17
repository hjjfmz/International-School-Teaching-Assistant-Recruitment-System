package ebu6304.ai.service;

import ebu6304.ai.dto.JobProfileSourceDto;
import ebu6304.ai.repository.AiDatasetRepository;
import ebu6304.ai.util.AiTextUtils;
import ebu6304.ai.vo.JobProfileVo;
import ebu6304.model.Job;

public final class JobProfileIndexService {
    private final AiDatasetRepository repository;
    private final JobParseService jobParseService;

    public JobProfileIndexService(AiDatasetRepository repository, JobParseService jobParseService) {
        this.repository = repository;
        this.jobParseService = jobParseService;
    }

    public JobProfileVo ensureProfile(Job job) {
        JobProfileSourceDto source = toSource(job);
        String hash = sourceHash(job);
        AiDatasetRepository.JobCacheEntry cached = repository == null || job == null ? null : repository.getJobProfile(job.id());
        if (cached != null && hash.equals(cached.sourceHash()) && cached.profile() != null) {
            return cached.profile();
        }
        JobProfileVo profile = jobParseService.parse(source);
        if (repository != null && job != null) repository.putJobProfile(job.id(), hash, profile);
        return profile;
    }

    public JobProfileVo getCachedOrLocalProfile(Job job) {
        String hash = sourceHash(job);
        AiDatasetRepository.JobCacheEntry cached = repository == null || job == null ? null : repository.getJobProfile(job.id());
        if (cached != null && hash.equals(cached.sourceHash()) && cached.profile() != null) {
            return cached.profile();
        }
        return jobParseService.parseLocal(toSource(job));
    }

    public JobProfileVo warmProfile(Job job) {
        return ensureProfile(job);
    }

    public String sourceHash(Job job) {
        if (job == null) return "";
        return AiTextUtils.sourceHash(job.title(), job.requiredSkills(), job.description(), String.valueOf(job.hoursPerWeek()));
    }

    private static JobProfileSourceDto toSource(Job job) {
        if (job == null) return new JobProfileSourceDto("", "", "", "", 0);
        return new JobProfileSourceDto(job.id(), job.title(), job.requiredSkills(), job.description(), job.hoursPerWeek());
    }
}
