package ebu6304.ai.prompt;

import ebu6304.ai.client.AiPrompt;
import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.ai.vo.JobProfileVo;

public final class JobMatchExplainPrompt {
    private JobMatchExplainPrompt() {}

    public static AiPrompt build(CandidateProfileVo candidate, JobProfileVo job, JobMatchResultVo match, boolean shortMode) {
        StringBuilder user = new StringBuilder();
        user.append("Explain a teaching-assistant job match result in strict JSON only.\n");
        user.append("Schema:\n");
        user.append("{");
        user.append("\"recommendTag\":\"short label\",");
        user.append("\"recommendReason\":\"one short sentence\",");
        user.append("\"recommendReasons\":[\"reason 1\",\"reason 2\",\"reason 3\"]");
        user.append("}\n");
        user.append("Rules:\n");
        user.append("- Base the explanation on the provided score breakdown; do not change scores.\n");
        user.append("- Keep the tag short and useful for UI cards.\n");
        if (shortMode) {
            user.append("- Keep recommendReason under 24 words.\n");
            user.append("- Keep recommendReasons to at most 2 concise bullets.\n");
        } else {
            user.append("- Give 2 to 4 concise and specific reasons.\n");
        }
        user.append("\nCandidate skills (from profile + resume): ").append(candidate == null ? "" : candidate.normalizedSkills()).append("\n");
        user.append("Candidate domains: ").append(candidate == null ? "" : candidate.domainTags()).append("\n");
        user.append("Candidate seniority: ").append(candidate == null ? "" : candidate.seniorityLevel().name()).append("\n");
        user.append("Candidate years of experience: ").append(candidate == null ? 0 : candidate.yearsExperience()).append("\n");
        user.append("Candidate summary: ").append(candidate == null ? "" : candidate.summary()).append("\n");
        user.append("Job title: ").append(job == null ? "" : job.title()).append("\n");
        user.append("Job skills: ").append(job == null ? "" : job.normalizedSkills()).append("\n");
        user.append("Job domains: ").append(job == null ? "" : job.domainTags()).append("\n");
        user.append("Score breakdown: overall=").append(match == null ? 0 : match.overallScore())
                .append(", skill=").append(match == null ? 0 : match.skillScore())
                .append(", seniority=").append(match == null ? 0 : match.seniorityScore())
                .append(", domain=").append(match == null ? 0 : match.domainScore()).append("\n");
        user.append("Matched skills: ").append(match == null ? "" : match.matchedSkills()).append("\n");
        user.append("Missing skills: ").append(match == null ? "" : match.missingSkills()).append("\n");
        return new AiPrompt(
                "You explain matching results for hiring workflows. Return JSON only.",
                user.toString(),
                shortMode ? 40 : 60);
    }
}
