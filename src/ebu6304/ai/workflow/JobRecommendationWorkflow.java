package ebu6304.ai.workflow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.repository.AiDatasetRepository;
import ebu6304.ai.service.CandidateProfileIndexService;
import ebu6304.ai.service.JobMatchExplainService;
import ebu6304.ai.service.JobProfileIndexService;
import ebu6304.ai.service.MatchScoreCalculator;
import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.ai.vo.JobProfileVo;
import ebu6304.ai.vo.JobRecommendationVo;
import ebu6304.ai.vo.RecommendationNoteVo;
import ebu6304.model.Applicant;
import ebu6304.model.Job;

public final class JobRecommendationWorkflow {
    private final AiDatasetRepository repository;
    private final CandidateProfileIndexService candidateIndexService;
    private final JobProfileIndexService jobIndexService;
    private final MatchScoreCalculator matchScoreCalculator;
    private final JobMatchExplainService explainService;

    public JobRecommendationWorkflow(AiDatasetRepository repository, CandidateProfileIndexService candidateIndexService,
            JobProfileIndexService jobIndexService, MatchScoreCalculator matchScoreCalculator,
            JobMatchExplainService explainService) {
        this.repository = repository;
        this.candidateIndexService = candidateIndexService;
        this.jobIndexService = jobIndexService;
        this.matchScoreCalculator = matchScoreCalculator;
        this.explainService = explainService;
    }

    public List<JobRecommendationVo> recommend(Applicant applicant, List<Job> jobs, int aiReasonTopN) {
        List<JobRecommendationVo> out = recommendFast(applicant, jobs);
        if (out.isEmpty() || applicant == null || aiReasonTopN <= 0) return out;

        int limit = Math.min(aiReasonTopN, out.size());
        for (int i = 0; i < limit; i++) {
            JobRecommendationVo enriched = enrichRecommendation(applicant, out.get(i).job(), null);
            if (enriched != null) out.set(i, enriched);
        }
        return out;
    }

    public List<JobRecommendationVo> recommendFast(Applicant applicant, List<Job> jobs) {
        List<JobRecommendationVo> out = new ArrayList<JobRecommendationVo>();
        if (applicant == null || jobs == null || jobs.isEmpty()) return out;

        CandidateProfileVo candidateProfile = candidateIndexService.getCachedOrLocalProfile(applicant);
        String candidateHash = candidateIndexService.sourceHash(applicant);

        List<JobScoreEntry> ranked = new ArrayList<JobScoreEntry>();
        for (Job job : jobs) {
            if (job == null) continue;
            JobProfileVo jobProfile = jobIndexService.getCachedOrLocalProfile(job);
            JobMatchResultVo match = matchScoreCalculator.calculate(candidateProfile, jobProfile);
            ranked.add(new JobScoreEntry(job, jobProfile, match, jobIndexService.sourceHash(job)));
        }
        Collections.sort(ranked, new Comparator<JobScoreEntry>() {
            @Override
            public int compare(JobScoreEntry a, JobScoreEntry b) {
                return Integer.compare(b.match.overallScore(), a.match.overallScore());
            }
        });

        for (JobScoreEntry entry : ranked) {
            RecommendationNoteVo note = explainService.fallback(candidateProfile, entry.profile, entry.match, candidateHash, entry.jobHash, true);
            out.add(new JobRecommendationVo(entry.job, entry.match.overallScore(), note.recommendTag(),
                    note.recommendReason(), entry.match.matchedSkills(), entry.match.missingSkills()));
        }
        return out;
    }

    public JobRecommendationVo enrichRecommendation(Applicant applicant, Job job, AiStreamListener listener) {
        if (applicant == null || job == null) return null;
        CandidateProfileVo candidateProfile = candidateIndexService.ensureProfile(applicant);
        JobProfileVo jobProfile = jobIndexService.ensureProfile(job);
        JobMatchResultVo match = matchScoreCalculator.calculate(candidateProfile, jobProfile);
        String candidateHash = candidateIndexService.sourceHash(applicant);
        String jobHash = jobIndexService.sourceHash(job);

        RecommendationNoteVo note = repository == null ? null : repository.getRecommendationNote(applicant.id(), job.id(), candidateHash, jobHash);
        if (note == null) {
            note = explainService.explain(candidateProfile, jobProfile, match, true, candidateHash, jobHash, listener);
            if (repository != null) repository.putRecommendationNote(note);
        }
        return new JobRecommendationVo(job, match.overallScore(), note.recommendTag(), note.recommendReason(),
                match.matchedSkills(), match.missingSkills());
    }

    private static final class JobScoreEntry {
        private final Job job;
        private final JobProfileVo profile;
        private final JobMatchResultVo match;
        private final String jobHash;

        private JobScoreEntry(Job job, JobProfileVo profile, JobMatchResultVo match, String jobHash) {
            this.job = job;
            this.profile = profile;
            this.match = match;
            this.jobHash = jobHash == null ? "" : jobHash;
        }
    }
}
