package ebu6304.ai.prompt;

import ebu6304.ai.client.AiPrompt;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.vo.JdQualityResultVo;
import ebu6304.ai.vo.JobProfileVo;

public final class JdPolishPrompt {
    private JdPolishPrompt() {}

    public static AiPrompt build(JobDraftDto draft, JobProfileVo normalized, JdQualityResultVo quality) {
        StringBuilder user = new StringBuilder();
        user.append("Polish this teaching-assistant job description and return strict JSON only.\n");
        user.append("Schema:\n");
        user.append("{");
        user.append("\"title\":\"polished title\",");
        user.append("\"requiredSkills\":\"normalized comma-separated skills\",");
        user.append("\"description\":\"polished description text\",");
        user.append("\"changeSummary\":[\"change 1\",\"change 2\"]");
        user.append("}\n");
        user.append("Rules:\n");
        user.append("- Preserve intent and weekly hours.\n");
        user.append("- Improve clarity, tone consistency, and candidate readability.\n");
        user.append("- Do not invent unsupported responsibilities.\n");
        user.append("- Use the review findings to improve the draft.\n\n");
        user.append("Original title: ").append(draft == null ? "" : draft.title()).append("\n");
        user.append("Original hours/week: ").append(draft == null ? 0 : draft.hoursPerWeek()).append("\n");
        user.append("Original skills: ").append(draft == null ? "" : draft.requiredSkills()).append("\n");
        user.append("Original description:\n").append(draft == null ? "" : draft.description()).append("\n\n");
        user.append("Normalized skills: ").append(normalized == null ? "" : normalized.normalizedSkills()).append("\n");
        user.append("Review summary: ").append(quality == null ? "" : quality.summary()).append("\n");
        user.append("Review suggestions: ").append(quality == null ? "" : quality.suggestions()).append("\n");
        return new AiPrompt(
                "You rewrite job descriptions for publication. Return JSON only.",
                user.toString(),
                60);
    }
}
