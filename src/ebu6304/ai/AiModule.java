package ebu6304.ai;

import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.DefaultAiClientFactory;
import ebu6304.ai.controller.AiIndexController;
import ebu6304.ai.controller.ApplicantMatchController;
import ebu6304.ai.controller.JdAssistantController;
import ebu6304.ai.controller.JobRecommendationController;
import ebu6304.ai.repository.AiDatasetRepository;
import ebu6304.ai.service.CandidateProfileIndexService;
import ebu6304.ai.service.JdPolishService;
import ebu6304.ai.service.JdQualityCheckService;
import ebu6304.ai.service.JobMatchExplainService;
import ebu6304.ai.service.JobParseService;
import ebu6304.ai.service.JobProfileIndexService;
import ebu6304.ai.service.MatchScoreCalculator;
import ebu6304.ai.service.ResumeParseService;
import ebu6304.ai.workflow.ApplicantJobMatchWorkflow;
import ebu6304.ai.workflow.JdOptimizationWorkflow;
import ebu6304.ai.workflow.JobRecommendationWorkflow;
import ebu6304.storage.DataService;

public final class AiModule {
    private final AiIndexController aiIndexController;
    private final ApplicantMatchController applicantMatchController;
    private final JdAssistantController jdAssistantController;
    private final JobRecommendationController jobRecommendationController;

    public AiModule(DataService data) {
        AiClientFactory aiClientFactory = new DefaultAiClientFactory();
        AiDatasetRepository repository = new AiDatasetRepository(data.dataDir().resolve("ai_dataset.json"));

        ResumeParseService resumeParseService = new ResumeParseService(aiClientFactory);
        JobParseService jobParseService = new JobParseService(aiClientFactory);
        CandidateProfileIndexService candidateIndexService = new CandidateProfileIndexService(repository, resumeParseService);
        JobProfileIndexService jobIndexService = new JobProfileIndexService(repository, jobParseService);
        MatchScoreCalculator matchScoreCalculator = new MatchScoreCalculator();
        JobMatchExplainService explainService = new JobMatchExplainService(aiClientFactory);
        JdQualityCheckService jdQualityCheckService = new JdQualityCheckService(aiClientFactory);
        JdPolishService jdPolishService = new JdPolishService(aiClientFactory);

        ApplicantJobMatchWorkflow applicantWorkflow = new ApplicantJobMatchWorkflow(candidateIndexService, jobIndexService, matchScoreCalculator, explainService);
        JdOptimizationWorkflow jdWorkflow = new JdOptimizationWorkflow(jobParseService, jdQualityCheckService, jdPolishService);
        JobRecommendationWorkflow recommendationWorkflow = new JobRecommendationWorkflow(repository, candidateIndexService, jobIndexService, matchScoreCalculator, explainService);

        this.aiIndexController = new AiIndexController(data, candidateIndexService, jobIndexService);
        this.applicantMatchController = new ApplicantMatchController(data, applicantWorkflow);
        this.jdAssistantController = new JdAssistantController(aiClientFactory, jdWorkflow);
        this.jobRecommendationController = new JobRecommendationController(data, recommendationWorkflow);
    }

    public AiIndexController aiIndexController() { return aiIndexController; }

    public ApplicantMatchController applicantMatchController() { return applicantMatchController; }

    public JdAssistantController jdAssistantController() { return jdAssistantController; }

    public JobRecommendationController jobRecommendationController() { return jobRecommendationController; }
}
