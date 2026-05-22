package ebu6304.ai.prompt;

import ebu6304.ai.client.AiPrompt;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.vo.JobProfileVo;

public final class JdQualityCheckPrompt {
    private JdQualityCheckPrompt() {}

    public static AiPrompt build(JobDraftDto draft, JobProfileVo normalized) {
        StringBuilder user = new StringBuilder();
        user.append("Review this teaching-assistant job description and return strict JSON only.\n");
        user.append("Schema:\n");
        user.append("{");
        user.append("\"overallScore\":0,");
        user.append("\"summary\":\"short overall assessment\",");
        user.append("\"issues\":[{\"dimension\":\"skills|clarity|missing-info|tone|compliance\",\"severity\":\"low|medium|high\",\"message\":\"issue text\"}],");
        user.append("\"suggestions\":[\"suggestion 1\",\"suggestion 2\"]");
        user.append("}\n");
        user.append("Review dimensions:\n");
        user.append("- Skill requirements too high or conflicting\n");
        user.append("- Responsibilities and expectations clear or unclear\n");
        user.append("- Missing key information\n");
        user.append("- Tone consistency\n");
        user.append("- Ambiguous or non-compliant phrasing\n\n");
        user.append("Draft title: ").append(draft == null ? "" : draft.title()).append("\n");
        user.append("Draft hours/week: ").append(draft == null ? 0 : draft.hoursPerWeek()).append("\n");
        user.append("Draft skills: ").append(draft == null ? "" : draft.requiredSkills()).append("\n");
        user.append("Draft description:\n").append(draft == null ? "" : draft.description()).append("\n\n");
        user.append("Normalized skills: ").append(normalized == null ? "" : normalized.normalizedSkills()).append("\n");
        user.append("Normalized domains: ").append(normalized == null ? "" : normalized.domainTags()).append("\n");
        return new AiPrompt(
                "You review job descriptions for quality and compliance. Return JSON only.",
                user.toString(),
                60);
    }
}
