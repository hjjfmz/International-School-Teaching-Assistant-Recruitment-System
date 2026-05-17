package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.List;

public final class JobMatchResultVo {
    private final String candidateId;
    private final String jobId;
    private final int overallScore;
    private final int skillScore;
    private final int seniorityScore;
    private final int domainScore;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;
    private final List<String> recommendReasons;
    private final String recommendTag;
    private final String shortReason;

    public JobMatchResultVo(String candidateId, String jobId, int overallScore, int skillScore, int seniorityScore,
            int domainScore, List<String> matchedSkills, List<String> missingSkills, List<String> recommendReasons,
            String recommendTag, String shortReason) {
        this.candidateId = candidateId == null ? "" : candidateId;
        this.jobId = jobId == null ? "" : jobId;
        this.overallScore = clamp(overallScore);
        this.skillScore = clamp(skillScore);
        this.seniorityScore = clamp(seniorityScore);
        this.domainScore = clamp(domainScore);
        this.matchedSkills = matchedSkills == null ? new ArrayList<String>() : new ArrayList<String>(matchedSkills);
        this.missingSkills = missingSkills == null ? new ArrayList<String>() : new ArrayList<String>(missingSkills);
        this.recommendReasons = recommendReasons == null ? new ArrayList<String>() : new ArrayList<String>(recommendReasons);
        this.recommendTag = recommendTag == null ? "" : recommendTag.trim();
        this.shortReason = shortReason == null ? "" : shortReason.trim();
    }

    public String candidateId() { return candidateId; }

    public String jobId() { return jobId; }

    public int overallScore() { return overallScore; }

    public int skillScore() { return skillScore; }

    public int seniorityScore() { return seniorityScore; }

    public int domainScore() { return domainScore; }

    public List<String> matchedSkills() { return new ArrayList<String>(matchedSkills); }

    public List<String> missingSkills() { return new ArrayList<String>(missingSkills); }

    public List<String> recommendReasons() { return new ArrayList<String>(recommendReasons); }

    public String recommendTag() { return recommendTag; }

    public String shortReason() { return shortReason; }

    public JobMatchResultVo withExplanation(List<String> reasons, String tag, String reason) {
        return new JobMatchResultVo(candidateId, jobId, overallScore, skillScore, seniorityScore, domainScore,
                matchedSkills, missingSkills, reasons, tag, reason);
    }

    private static int clamp(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }
}
