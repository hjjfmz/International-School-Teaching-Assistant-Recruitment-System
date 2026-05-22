package ebu6304.ai.workflow;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.service.CandidateProfileIndexService;
import ebu6304.ai.service.JobMatchExplainService;
import ebu6304.ai.service.JobProfileIndexService;
import ebu6304.ai.service.MatchScoreCalculator;
import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.ai.vo.JobProfileVo;
import ebu6304.ai.vo.RecommendationNoteVo;
import ebu6304.model.Applicant;
import ebu6304.model.Job;

public final class ApplicantJobMatchWorkflow {
    private final CandidateProfileIndexService candidateIndexService;
    private final JobProfileIndexService jobIndexService;
    private final MatchScoreCalculator matchScoreCalculator;
    private final JobMatchExplainService explainService;

    public ApplicantJobMatchWorkflow(CandidateProfileIndexService candidateIndexService,
            JobProfileIndexService jobIndexService,
            MatchScoreCalculator matchScoreCalculator,
            JobMatchExplainService explainService) {
        this.candidateIndexService = candidateIndexService;
        this.jobIndexService = jobIndexService;
        this.matchScoreCalculator = matchScoreCalculator;
        this.explainService = explainService;
    }

    public JobMatchResultVo evaluate(Applicant applicant, Job job, boolean includeExplanation) {
        return evaluate(applicant, job, includeExplanation, null);
    }

    public JobMatchResultVo evaluate(Applicant applicant, Job job, boolean includeExplanation, AiStreamListener listener) {
        CandidateProfileVo candidateProfile = candidateIndexService.ensureProfile(applicant);
        JobProfileVo jobProfile = jobIndexService.ensureProfile(job);
        JobMatchResultVo result = matchScoreCalculator.calculate(candidateProfile, jobProfile);
        if (!includeExplanation) return result;

        String candidateHash = candidateIndexService.sourceHash(applicant);
        String jobHash = jobIndexService.sourceHash(job);
        RecommendationNoteVo note = explainService.explain(candidateProfile, jobProfile, result, false, candidateHash, jobHash, listener);
        return result.withExplanation(note.recommendReasons(), note.recommendTag(), note.recommendReason());
    }

    public JobMatchResultVo evaluateFast(Applicant applicant, Job job) {
        CandidateProfileVo candidateProfile = candidateIndexService.getCachedOrLocalProfile(applicant);
        JobProfileVo jobProfile = jobIndexService.getCachedOrLocalProfile(job);
        JobMatchResultVo result = matchScoreCalculator.calculate(candidateProfile, jobProfile);
        String candidateHash = candidateIndexService.sourceHash(applicant);
        String jobHash = jobIndexService.sourceHash(job);
        RecommendationNoteVo note = explainService.fallback(candidateProfile, jobProfile, result, candidateHash, jobHash, false);
        return result.withExplanation(note.recommendReasons(), note.recommendTag(), note.recommendReason());
    }
}
