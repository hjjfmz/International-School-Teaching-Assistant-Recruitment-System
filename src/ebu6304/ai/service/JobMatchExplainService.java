package ebu6304.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ebu6304.ai.client.AiChatClient;
import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.AiJsonSupport;
import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.prompt.JobMatchExplainPrompt;
import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.ai.vo.JobProfileVo;
import ebu6304.ai.vo.RecommendationNoteVo;

public final class JobMatchExplainService {
    private final AiClientFactory aiClientFactory;

    public JobMatchExplainService(AiClientFactory aiClientFactory) {
        this.aiClientFactory = aiClientFactory;
    }

    public RecommendationNoteVo explain(CandidateProfileVo candidate, JobProfileVo job, JobMatchResultVo match, boolean shortMode,
            String candidateHash, String jobHash) {
        return explain(candidate, job, match, shortMode, candidateHash, jobHash, null);
    }

    public RecommendationNoteVo explain(CandidateProfileVo candidate, JobProfileVo job, JobMatchResultVo match, boolean shortMode,
            String candidateHash, String jobHash, AiStreamListener listener) {
        RecommendationNoteVo fallback = fallback(candidate, job, match, candidateHash, jobHash, shortMode);
        if (aiClientFactory == null || !aiClientFactory.isConfigured()) return fallback;
        try {
            AiChatClient client = aiClientFactory.createChatClient();
            String content = listener == null
                    ? client.chat(JobMatchExplainPrompt.build(candidate, job, match, shortMode))
                    : client.chatStream(JobMatchExplainPrompt.build(candidate, job, match, shortMode), listener);
            Map<String, Object> root = AiJsonSupport.parseObject(content);
            List<String> reasons = mergeReasons(AiJsonSupport.readStringList(root, "recommendReasons"), fallback.recommendReasons());
            String tag = preferNonBlank(AiJsonSupport.readString(root, "recommendTag"), fallback.recommendTag());
            String reason = preferNonBlank(AiJsonSupport.readString(root, "recommendReason"), fallback.recommendReason());
            return new RecommendationNoteVo(
                    candidate == null ? "" : candidate.candidateId(),
                    job == null ? "" : job.jobId(),
                    candidateHash,
                    jobHash,
                    tag,
                    reason,
                    reasons);
        } catch (Exception ex) {
            return fallback;
        }
    }

    public RecommendationNoteVo fallback(CandidateProfileVo candidate, JobProfileVo job, JobMatchResultVo match,
            String candidateHash, String jobHash, boolean shortMode) {
        List<String> reasons = new ArrayList<String>();
        if (match != null && !match.matchedSkills().isEmpty()) {
            reasons.add("Matched skills (profile + resume): " + join(match.matchedSkills(), shortMode ? 3 : 5));
        }
        if (match != null && match.seniorityScore() >= 90) {
            reasons.add("Experience level is aligned with the role.");
        } else if (match != null && match.seniorityScore() <= 50) {
            String yearsInfo = candidate != null && candidate.yearsExperience() > 0
                    ? " (resume indicates ~" + candidate.yearsExperience() + " years)"
                    : "";
            reasons.add("Seniority looks lighter than the role expectation." + yearsInfo);
        }
        if (candidate != null && candidate.yearsExperience() > 0 && match != null && match.seniorityScore() >= 65) {
            reasons.add("Resume indicates ~" + candidate.yearsExperience() + " years of relevant experience.");
        }
        if (match != null && !match.missingSkills().isEmpty()) {
            reasons.add("Missing skills: " + join(match.missingSkills(), shortMode ? 2 : 4));
        }
        if (reasons.isEmpty()) {
            reasons.add("Profile overlap is moderate and needs manual review.");
        }

        String tag;
        int score = match == null ? 0 : match.overallScore();
        if (score >= 85) tag = "Most compatible";
        else if (score >= 70) tag = "Skills highly aligned";
        else if (match != null && match.missingSkills().size() <= 1) tag = "One skill away";
        else tag = "Needs review";

        String shortReason = reasons.get(0);
        return new RecommendationNoteVo(
                candidate == null ? "" : candidate.candidateId(),
                job == null ? "" : job.jobId(),
                candidateHash,
                jobHash,
                tag,
                shortReason,
                reasons);
    }

    private static List<String> mergeReasons(List<String> preferred, List<String> fallback) {
        List<String> out = new ArrayList<String>();
        if (preferred != null) {
            for (String item : preferred) {
                if (item != null && !item.trim().isEmpty()) out.add(item.trim());
            }
        }
        if (out.isEmpty() && fallback != null) out.addAll(fallback);
        return out;
    }

    private static String preferNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) return preferred.trim();
        return fallback == null ? "" : fallback.trim();
    }

    private static String join(List<String> values, int max) {
        if (values == null || values.isEmpty()) return "";
        List<String> out = new ArrayList<String>();
        for (String value : values) {
            if (value == null || value.trim().isEmpty()) continue;
            out.add(value.trim());
            if (out.size() >= max) break;
        }
        return String.join(", ", out);
    }
}
