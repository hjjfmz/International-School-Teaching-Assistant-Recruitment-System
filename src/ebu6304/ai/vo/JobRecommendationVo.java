package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.List;

import ebu6304.model.Job;

public final class JobRecommendationVo {
    private final Job job;
    private final int matchScore;
    private final String recommendTag;
    private final String recommendReason;
    private final List<String> matchedSkills;
    private final List<String> missingSkills;

    public JobRecommendationVo(Job job, int matchScore, String recommendTag, String recommendReason,
            List<String> matchedSkills, List<String> missingSkills) {
        this.job = job;
        this.matchScore = matchScore < 0 ? 0 : Math.min(100, matchScore);
        this.recommendTag = recommendTag == null ? "" : recommendTag.trim();
        this.recommendReason = recommendReason == null ? "" : recommendReason.trim();
        this.matchedSkills = matchedSkills == null ? new ArrayList<String>() : new ArrayList<String>(matchedSkills);
        this.missingSkills = missingSkills == null ? new ArrayList<String>() : new ArrayList<String>(missingSkills);
    }

    public Job job() { return job; }

    public int matchScore() { return matchScore; }

    public String recommendTag() { return recommendTag; }

    public String recommendReason() { return recommendReason; }

    public List<String> matchedSkills() { return new ArrayList<String>(matchedSkills); }

    public List<String> missingSkills() { return new ArrayList<String>(missingSkills); }
}
