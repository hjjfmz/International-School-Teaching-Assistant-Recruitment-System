package ebu6304.ai.prompt;

import ebu6304.ai.client.AiPrompt;
import ebu6304.ai.dto.JobProfileSourceDto;
import ebu6304.ai.util.AiTextUtils;

public final class JobProfilePrompt {
    private JobProfilePrompt() {}

    public static AiPrompt build(JobProfileSourceDto source) {
        StringBuilder user = new StringBuilder();
        user.append("Normalize this teaching-assistant job description into strict JSON only.\n");
        user.append("Schema:\n");
        user.append("{");
        user.append("\"normalizedSkills\":[\"skill1\",\"skill2\"],");
        user.append("\"domainTags\":[\"domain1\",\"domain2\"],");
        user.append("\"seniorityLevel\":\"UNKNOWN|JUNIOR|MID|SENIOR|LEAD\",");
        user.append("\"keywords\":[\"keyword1\",\"keyword2\"],");
        user.append("\"summary\":\"one short summary\"");
        user.append("}\n");
        user.append("Rules:\n");
        user.append("- Normalize skills to concise lower-case tokens.\n");
        user.append("- domainTags should be reusable for matching and recommendation.\n");
        user.append("- Infer seniority expectations conservatively.\n");
        user.append("- Return JSON only.\n\n");
        user.append("Job ID: ").append(source == null ? "" : source.jobId()).append("\n");
        user.append("Title: ").append(source == null ? "" : source.title()).append("\n");
        user.append("Hours per week: ").append(source == null ? 0 : source.hoursPerWeek()).append("\n");
        user.append("Required skills:\n").append(source == null ? "" : AiTextUtils.shorten(source.requiredSkillsText(), 1600)).append("\n\n");
        user.append("Description:\n").append(source == null ? "" : AiTextUtils.shorten(source.descriptionText(), 5000)).append("\n");
        return new AiPrompt(
                "You extract structured job profiles for downstream scoring. Return JSON only.",
                user.toString(),
                60);
    }
}
