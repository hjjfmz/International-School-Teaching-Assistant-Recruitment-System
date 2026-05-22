package ebu6304.ai.controller;

import java.util.ArrayList;
import java.util.List;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.workflow.JobRecommendationWorkflow;
import ebu6304.ai.vo.JobRecommendationVo;
import ebu6304.model.Applicant;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class JobRecommendationController {
    private final DataService data;
    private final JobRecommendationWorkflow workflow;

    public JobRecommendationController(DataService data, JobRecommendationWorkflow workflow) {
        this.data = data;
        this.workflow = workflow;
    }

    public List<JobRecommendationVo> recommendJobs(String applicantId, int aiReasonTopN) {
        if (data == null || applicantId == null || applicantId.trim().isEmpty()) return new ArrayList<JobRecommendationVo>();
        Applicant applicant = data.getApplicant(applicantId).orElse(null);
        List<Job> jobs = data.listJobs();
        if (applicant == null) return new ArrayList<JobRecommendationVo>();
        return workflow.recommend(applicant, jobs, aiReasonTopN);
    }

    public List<JobRecommendationVo> recommendJobsFast(String applicantId) {
        if (data == null || applicantId == null || applicantId.trim().isEmpty()) return new ArrayList<JobRecommendationVo>();
        Applicant applicant = data.getApplicant(applicantId).orElse(null);
        List<Job> jobs = data.listJobs();
        if (applicant == null) return new ArrayList<JobRecommendationVo>();
        return workflow.recommendFast(applicant, jobs);
    }

    public JobRecommendationVo enrichRecommendation(String applicantId, String jobId, AiStreamListener listener) {
        if (data == null || applicantId == null || applicantId.trim().isEmpty() || jobId == null || jobId.trim().isEmpty()) {
            return null;
        }
        Applicant applicant = data.getApplicant(applicantId).orElse(null);
        Job job = data.getJob(jobId).orElse(null);
        if (applicant == null || job == null) return null;
        return workflow.enrichRecommendation(applicant, job, listener);
    }
}
