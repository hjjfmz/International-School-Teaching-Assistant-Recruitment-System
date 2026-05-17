package ebu6304.ai.controller;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.workflow.ApplicantJobMatchWorkflow;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.model.Applicant;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class ApplicantMatchController {
    private final DataService data;
    private final ApplicantJobMatchWorkflow workflow;

    public ApplicantMatchController(DataService data, ApplicantJobMatchWorkflow workflow) {
        this.data = data;
        this.workflow = workflow;
    }

    public JobMatchResultVo evaluate(Job job, Applicant applicant, String applicationId, boolean includeExplanation) {
        JobMatchResultVo result = workflow.evaluate(applicant, job, includeExplanation);
        if (data != null && applicationId != null && !applicationId.trim().isEmpty()) {
            data.updateApplicationAiScore(applicationId, result.overallScore());
        }
        return result;
    }

    public JobMatchResultVo evaluateFast(Job job, Applicant applicant) {
        return workflow.evaluateFast(applicant, job);
    }

    public JobMatchResultVo evaluateWithStreaming(Job job, Applicant applicant, String applicationId, AiStreamListener listener) {
        JobMatchResultVo result = workflow.evaluate(applicant, job, true, listener);
        if (data != null && applicationId != null && !applicationId.trim().isEmpty()) {
            data.updateApplicationAiScore(applicationId, result.overallScore());
        }
        return result;
    }
}
