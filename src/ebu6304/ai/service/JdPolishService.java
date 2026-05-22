package ebu6304.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ebu6304.ai.client.AiChatClient;
import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.AiJsonSupport;
import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.prompt.JdPolishPrompt;
import ebu6304.ai.util.AiTextUtils;
import ebu6304.ai.vo.JdPolishResultVo;
import ebu6304.ai.vo.JdQualityResultVo;
import ebu6304.ai.vo.JobProfileVo;

public final class JdPolishService {
    private final AiClientFactory aiClientFactory;

    public JdPolishService(AiClientFactory aiClientFactory) {
        this.aiClientFactory = aiClientFactory;
    }

    public JdPolishResultVo polish(JobDraftDto draft, JobProfileVo normalized, JdQualityResultVo quality) {
        return polish(draft, normalized, quality, null);
    }

    public JdPolishResultVo polish(JobDraftDto draft, JobProfileVo normalized, JdQualityResultVo quality, AiStreamListener listener) {
        JdPolishResultVo fallback = heuristic(draft, normalized);
        if (draft == null || aiClientFactory == null || !aiClientFactory.isConfigured()) return fallback;
        try {
            AiChatClient client = aiClientFactory.createChatClient();
            String content = listener == null
                    ? client.chat(JdPolishPrompt.build(draft, normalized, quality))
                    : client.chatStream(JdPolishPrompt.build(draft, normalized, quality), listener);
            Map<String, Object> root = AiJsonSupport.parseObject(content);
            List<String> changes = AiJsonSupport.readStringList(root, "changeSummary");
            if (changes.isEmpty()) changes = fallback.changeSummary();
            return new JdPolishResultVo(
                    preferNonBlank(AiJsonSupport.readString(root, "title"), fallback.title()),
                    preferNonBlank(AiJsonSupport.readString(root, "requiredSkills"), fallback.requiredSkills()),
                    preferNonBlank(AiJsonSupport.readString(root, "description"), fallback.description()),
                    changes);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private JdPolishResultVo heuristic(JobDraftDto draft, JobProfileVo normalized) {
        if (draft == null) return new JdPolishResultVo("", "", "", null);
        List<String> changes = new ArrayList<String>();
        String title = draft.title().replaceAll("\\s+", " ").trim();
        String requiredSkills = normalized != null && !normalized.normalizedSkills().isEmpty()
                ? String.join(", ", normalized.normalizedSkills())
                : draft.requiredSkills().replaceAll("\\s*,\\s*", ", ").trim();
        String description = AiTextUtils.normalizeWhitespace(draft.description());
        if (!description.isEmpty() && !description.endsWith(".")) description = description + ".";
        description = description.replaceAll("\\. ", ".\n\n");
        changes.add("Normalized spacing and formatting.");
        if (!requiredSkills.equals(draft.requiredSkills())) changes.add("Standardized the required skills list.");
        if (!description.equals(draft.description())) changes.add("Improved description readability for publication.");
        return new JdPolishResultVo(title, requiredSkills, description, changes);
    }

    private static String preferNonBlank(String preferred, String fallback) {
        if (preferred != null && !preferred.trim().isEmpty()) return preferred.trim();
        return fallback == null ? "" : fallback.trim();
    }
}
