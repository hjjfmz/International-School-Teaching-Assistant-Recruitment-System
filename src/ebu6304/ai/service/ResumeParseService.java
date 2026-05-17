package ebu6304.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ebu6304.ai.client.AiChatClient;
import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.AiJsonSupport;
import ebu6304.ai.dto.CandidateProfileSourceDto;
import ebu6304.ai.prompt.CandidateProfilePrompt;
import ebu6304.ai.util.AiTextUtils;
import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.SeniorityLevel;

public final class ResumeParseService {
    private final AiClientFactory aiClientFactory;

    public ResumeParseService(AiClientFactory aiClientFactory) {
        this.aiClientFactory = aiClientFactory;
    }

    public CandidateProfileVo parse(CandidateProfileSourceDto source) {
        CandidateProfileVo fallback = heuristic(source);
        if (source == null || aiClientFactory == null || !aiClientFactory.isConfigured()) return fallback;
        try {
            AiChatClient client = aiClientFactory.createChatClient();
            Map<String, Object> root = AiJsonSupport.parseObject(client.chat(CandidateProfilePrompt.build(source)));
            CandidateProfileVo extracted = new CandidateProfileVo(
                    source.candidateId(),
                    AiTextUtils.mergeUnique(AiJsonSupport.readStringList(root, "normalizedSkills"), fallback.normalizedSkills()),
                    AiTextUtils.mergeUnique(AiJsonSupport.readStringList(root, "domainTags"), fallback.domainTags()),
                    preferredLevel(AiJsonSupport.readString(root, "seniorityLevel"), fallback.seniorityLevel()),
                    Math.max(AiJsonSupport.readInt(root, "yearsExperience"), fallback.yearsExperience()),
                    preferNonBlank(AiJsonSupport.readString(root, "summary"), fallback.summary()));
            return extracted;
        } catch (Exception ex) {
            return fallback;
        }
    }

    public CandidateProfileVo parseLocal(CandidateProfileSourceDto source) {
        return heuristic(source);
    }

    private CandidateProfileVo heuristic(CandidateProfileSourceDto source) {
        if (source == null) return new CandidateProfileVo("", null, null, SeniorityLevel.UNKNOWN, 0, "");
        List<String> skills = AiTextUtils.extractSkills(source.skillsText(), source.summaryText());
        List<String> resumeSkills = AiTextUtils.extractSkills(source.resumeText());
        skills = AiTextUtils.mergeUnique(skills, trimSkillList(resumeSkills, 18));
        List<String> domains = AiTextUtils.detectDomains(source.skillsText(), source.summaryText(), source.resumeText());
        SeniorityLevel level = AiTextUtils.detectSeniority(source.summaryText(), source.resumeText());
        int years = AiTextUtils.detectYearsExperience(source.summaryText(), source.resumeText());
        String summary = preferNonBlank(AiTextUtils.shorten(source.summaryText(), 220), buildSummary(skills, domains));
        return new CandidateProfileVo(source.candidateId(), skills, domains, level, years, summary);
    }

    private static List<String> trimSkillList(List<String> skills, int max) {
        List<String> out = new ArrayList<String>();
        if (skills == null) return out;
        for (String skill : skills) {
            if (skill == null || skill.trim().isEmpty()) continue;
            out.add(skill);
            if (out.size() >= max) break;
        }
        return out;
    }

    private static SeniorityLevel preferredLevel(String text, SeniorityLevel fallback) {
        SeniorityLevel parsed = SeniorityLevel.fromText(text);
        return parsed == SeniorityLevel.UNKNOWN ? fallback : parsed;
    }

    private static String preferNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) return preferred.trim();
        return fallback == null ? "" : fallback.trim();
    }

    private static String buildSummary(List<String> skills, List<String> domains) {
        StringBuilder sb = new StringBuilder();
        if (skills != null && !skills.isEmpty()) {
            sb.append("Skills: ").append(skills.subList(0, Math.min(4, skills.size())));
        }
        if (domains != null && !domains.isEmpty()) {
            if (sb.length() > 0) sb.append(". ");
            sb.append("Domains: ").append(domains);
        }
        return sb.toString();
    }
}
