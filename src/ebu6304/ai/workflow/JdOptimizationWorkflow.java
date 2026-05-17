package ebu6304.ai.workflow;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.dto.JobProfileSourceDto;
import ebu6304.ai.service.JdPolishService;
import ebu6304.ai.service.JdQualityCheckService;
import ebu6304.ai.service.JobParseService;
import ebu6304.ai.vo.JdPolishResultVo;
import ebu6304.ai.vo.JdQualityResultVo;
import ebu6304.ai.vo.JobProfileVo;

public final class JdOptimizationWorkflow {
    private final JobParseService jobParseService;
    private final JdQualityCheckService qualityCheckService;
    private final JdPolishService polishService;

    public JdOptimizationWorkflow(JobParseService jobParseService, JdQualityCheckService qualityCheckService,
            JdPolishService polishService) {
        this.jobParseService = jobParseService;
        this.qualityCheckService = qualityCheckService;
        this.polishService = polishService;
    }

    public JdQualityResultVo review(JobDraftDto draft) {
        return review(draft, null);
    }

    public JdQualityResultVo review(JobDraftDto draft, AiStreamListener listener) {
        JobProfileVo normalized = normalize(draft);
        return qualityCheckService.check(draft, normalized, listener);
    }

    public JdPolishResultVo polish(JobDraftDto draft) {
        return polish(draft, null);
    }

    public JdPolishResultVo polish(JobDraftDto draft, AiStreamListener listener) {
        JobProfileVo normalized = normalize(draft);
        JdQualityResultVo quality = qualityCheckService.check(draft, normalized);
        return polishService.polish(draft, normalized, quality, listener);
    }

    private JobProfileVo normalize(JobDraftDto draft) {
        return jobParseService.parse(new JobProfileSourceDto("", draft == null ? "" : draft.title(),
                draft == null ? "" : draft.requiredSkills(),
                draft == null ? "" : draft.description(),
                draft == null ? 0 : draft.hoursPerWeek()));
    }
}
