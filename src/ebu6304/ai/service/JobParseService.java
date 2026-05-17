package ebu6304.ai.service;

import java.util.Map;

import ebu6304.ai.client.AiChatClient;
import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.AiJsonSupport;
import ebu6304.ai.dto.JobProfileSourceDto;
import ebu6304.ai.prompt.JobProfilePrompt;
import ebu6304.ai.util.AiTextUtils;
import ebu6304.ai.vo.JobProfileVo;
import ebu6304.ai.vo.SeniorityLevel;

public final class JobParseService {
    private final AiClientFactory aiClientFactory;

    public JobParseService(AiClientFactory aiClientFactory) {
        this.aiClientFactory = aiClientFactory;
    }

    public JobProfileVo parse(JobProfileSourceDto source) {
        JobProfileVo fallback = heuristic(source);
        if (source == null || aiClientFactory == null || !aiClientFactory.isConfigured()) return fallback;
        try {
            AiChatClient client = aiClientFactory.createChatClient();
            Map<String, Object> root = AiJsonSupport.parseObject(client.chat(JobProfilePrompt.build(source)));
            return new JobProfileVo(
                    source.jobId(),
                    preferNonBlank(AiJsonSupport.readString(root, "title"), fallback.title()),
                    AiTextUtils.mergeUnique(AiJsonSupport.readStringList(root, "normalizedSkills"), fallback.normalizedSkills()),
                    AiTextUtils.mergeUnique(AiJsonSupport.readStringList(root, "domainTags"), fallback.domainTags()),
                    preferredLevel(AiJsonSupport.readString(root, "seniorityLevel"), fallback.seniorityLevel()),
                    source.hoursPerWeek(),
                    AiTextUtils.mergeUnique(AiJsonSupport.readStringList(root, "keywords"), fallback.keywords()),
                    preferNonBlank(AiJsonSupport.readString(root, "summary"), fallback.summary()));
        } catch (Exception ex) {
            return fallback;
        }
    }

    public JobProfileVo parseLocal(JobProfileSourceDto source) {
        return heuristic(source);
    }

    private JobProfileVo heuristic(JobProfileSourceDto source) {
        if (source == null) return new JobProfileVo("", "", null, null, SeniorityLevel.UNKNOWN, 0, null, "");
        return new JobProfileVo(
                source.jobId(),
                source.title(),
                AiTextUtils.extractSkills(source.requiredSkillsText(), source.title()),
                AiTextUtils.detectDomains(source.title(), source.requiredSkillsText(), source.descriptionText()),
                AiTextUtils.detectSeniority(source.title(), source.descriptionText()),
                source.hoursPerWeek(),
                AiTextUtils.extractSkills(source.title(), source.descriptionText()),
                AiTextUtils.shorten(source.descriptionText(), 220));
    }

    private static SeniorityLevel preferredLevel(String text, SeniorityLevel fallback) {
        SeniorityLevel parsed = SeniorityLevel.fromText(text);
        return parsed == SeniorityLevel.UNKNOWN ? fallback : parsed;
    }

    private static String preferNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) return preferred.trim();
        return fallback == null ? "" : fallback.trim();
    }
}
