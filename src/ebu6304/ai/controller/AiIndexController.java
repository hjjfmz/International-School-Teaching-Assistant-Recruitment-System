package ebu6304.ai.controller;

import ebu6304.ai.service.CandidateProfileIndexService;
import ebu6304.ai.service.JobProfileIndexService;
import ebu6304.model.Applicant;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class AiIndexController {
    private final DataService data;
    private final CandidateProfileIndexService candidateIndexService;
    private final JobProfileIndexService jobIndexService;

    public AiIndexController(DataService data, CandidateProfileIndexService candidateIndexService,
            JobProfileIndexService jobIndexService) {
        this.data = data;
        this.candidateIndexService = candidateIndexService;
        this.jobIndexService = jobIndexService;
    }

    public void refreshApplicant(String applicantId) {
        if (data == null || applicantId == null || applicantId.trim().isEmpty()) return;
        Applicant applicant = data.getApplicant(applicantId).orElse(null);
        if (applicant != null) {
            Thread t = new Thread(() -> candidateIndexService.warmProfile(applicant), "ai-warm-applicant-" + applicant.id());
            t.setDaemon(true);
            t.start();
        }
    }

    public void refreshJob(String jobId) {
        if (data == null || jobId == null || jobId.trim().isEmpty()) return;
        Job job = data.getJob(jobId).orElse(null);
        if (job != null) {
            Thread t = new Thread(() -> jobIndexService.warmProfile(job), "ai-warm-job-" + job.id());
            t.setDaemon(true);
            t.start();
        }
    }
}
