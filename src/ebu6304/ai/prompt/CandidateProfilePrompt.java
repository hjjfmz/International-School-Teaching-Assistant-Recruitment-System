package ebu6304.ai.prompt;

import ebu6304.ai.client.AiPrompt;
import ebu6304.ai.dto.CandidateProfileSourceDto;
import ebu6304.ai.util.AiTextUtils;

public final class CandidateProfilePrompt {
    private CandidateProfilePrompt() {}

    public static AiPrompt build(CandidateProfileSourceDto source) {
        StringBuilder user = new StringBuilder();
        user.append("Normalize this teaching-assistant candidate profile into strict JSON only.\n");
        user.append("Schema:\n");
        user.append("{");
        user.append("\"normalizedSkills\":[\"skill1\",\"skill2\"],");
        user.append("\"domainTags\":[\"domain1\",\"domain2\"],");
        user.append("\"seniorityLevel\":\"UNKNOWN|JUNIOR|MID|SENIOR|LEAD\",");
        user.append("\"yearsExperience\":0,");
        user.append("\"summary\":\"one short summary\"");
        user.append("}\n");
        user.append("Rules:\n");
        user.append("- Normalize skills to concise lower-case tokens.\n");
        user.append("- Infer only from the provided content; do not invent certifications or experience.\n");
        user.append("- domainTags should be concise and reusable for matching.\n");
        user.append("- seniorityLevel must be one of the enum values.\n\n");
        user.append("Candidate ID: ").append(source == null ? "" : source.candidateId()).append("\n");
        user.append("Self-described skills:\n").append(source == null ? "" : AiTextUtils.shorten(source.skillsText(), 1200)).append("\n\n");
        user.append("Profile summary:\n").append(source == null ? "" : AiTextUtils.shorten(source.summaryText(), 1600)).append("\n\n");
        user.append("Resume text:\n").append(source == null ? "" : AiTextUtils.shorten(source.resumeText(), 5000)).append("\n");
        return new AiPrompt(
                "You extract structured candidate profiles for downstream scoring. Return JSON only.",
                user.toString(),
                60);
    }
}
