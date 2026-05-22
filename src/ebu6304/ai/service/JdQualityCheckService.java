package ebu6304.ai.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ebu6304.ai.client.AiChatClient;
import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.AiJsonSupport;
import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.prompt.JdQualityCheckPrompt;
import ebu6304.ai.vo.JdQualityIssueVo;
import ebu6304.ai.vo.JdQualityResultVo;
import ebu6304.ai.vo.JobProfileVo;

public final class JdQualityCheckService {
    private final AiClientFactory aiClientFactory;

    public JdQualityCheckService(AiClientFactory aiClientFactory) {
        this.aiClientFactory = aiClientFactory;
    }

    public JdQualityResultVo check(JobDraftDto draft, JobProfileVo normalized) {
        return check(draft, normalized, null);
    }

    public JdQualityResultVo check(JobDraftDto draft, JobProfileVo normalized, AiStreamListener listener) {
        JdQualityResultVo fallback = heuristic(draft, normalized);
        if (draft == null || aiClientFactory == null || !aiClientFactory.isConfigured()) return fallback;
        try {
            AiChatClient client = aiClientFactory.createChatClient();
            String content = listener == null
                    ? client.chat(JdQualityCheckPrompt.build(draft, normalized))
                    : client.chatStream(JdQualityCheckPrompt.build(draft, normalized), listener);
            Map<String, Object> root = AiJsonSupport.parseObject(content);
            List<JdQualityIssueVo> issues = parseIssues(root);
            if (issues.isEmpty()) issues = fallback.issues();
            List<String> suggestions = AiJsonSupport.readStringList(root, "suggestions");
            if (suggestions.isEmpty()) suggestions = fallback.suggestions();
            int score = AiJsonSupport.readInt(root, "overallScore");
            if (score <= 0) score = fallback.overallScore();
            String summary = AiJsonSupport.readString(root, "summary");
            if (summary.isEmpty()) summary = fallback.summary();
            return new JdQualityResultVo(score, summary, issues, suggestions);
        } catch (Exception ex) {
            return fallback;
        }
    }

    private JdQualityResultVo heuristic(JobDraftDto draft, JobProfileVo normalized) {
        List<JdQualityIssueVo> issues = new ArrayList<JdQualityIssueVo>();
        List<String> suggestions = new ArrayList<String>();
        int penalty = 0;

        int skillCount = normalized == null ? 0 : normalized.normalizedSkills().size();
        int hours = draft == null ? 0 : draft.hoursPerWeek();
        String description = draft == null ? "" : draft.description();
        String requiredSkills = draft == null ? "" : draft.requiredSkills();
        String fullText = (draft == null ? "" : draft.title()) + "\n" + description + "\n" + requiredSkills;
        String lower = fullText.toLowerCase();

        if (skillCount >= 8 && hours > 0 && hours <= 6) {
            issues.add(new JdQualityIssueVo("skills", "high", "The role asks for many skills relative to the weekly hours."));
            suggestions.add("Reduce or prioritize the must-have skills for a part-time TA role.");
            penalty += 18;
        }
        if (lower.contains("senior") && lower.contains("entry")) {
            issues.add(new JdQualityIssueVo("skills", "medium", "The draft mixes senior and entry-level expectations."));
            suggestions.add("Clarify the target experience level.");
            penalty += 10;
        }
        if (description.trim().length() < 80) {
            issues.add(new JdQualityIssueVo("clarity", "high", "The job description is too short to explain responsibilities clearly."));
            suggestions.add("Add concrete responsibilities, working style, and expected outcomes.");
            penalty += 18;
        }
        if (!containsAny(lower, "responsib", "support", "assist", "mark", "lab", "tutorial", "feedback")) {
            issues.add(new JdQualityIssueVo("missing-info", "medium", "The draft does not clearly describe the actual TA responsibilities."));
            suggestions.add("Describe what the TA will support each week.");
            penalty += 12;
        }
        if (!containsAny(lower, "week", "hours", "schedule", "online", "in-person", "hybrid")) {
            issues.add(new JdQualityIssueVo("missing-info", "low", "Working arrangement or schedule detail is missing."));
            suggestions.add("Mention schedule expectations or delivery mode if known.");
            penalty += 6;
        }
        if (containsAny(lower, "!!!", "must be native", "young", "male only", "female only")) {
            issues.add(new JdQualityIssueVo("compliance", "high", "The draft may contain tone or phrasing that needs compliance review."));
            suggestions.add("Remove subjective or potentially discriminatory phrasing.");
            penalty += 20;
        }
        if (containsAny(lower, "!!!", "urgent!!!", "best of the best")) {
            issues.add(new JdQualityIssueVo("tone", "medium", "The tone feels uneven for an academic job post."));
            suggestions.add("Use a consistent and professional tone.");
            penalty += 8;
        }

        int score = 100 - penalty;
        if (score < 35) score = 35;
        String summary;
        if (issues.isEmpty()) {
            summary = "The draft is solid and publishable, with only minor refinement opportunities.";
            suggestions.add("Add one sentence about the typical weekly responsibilities for even better clarity.");
        } else if (score >= 75) {
            summary = "The draft is workable but needs a few refinements before publication.";
        } else {
            summary = "The draft needs clearer scope and tighter expectations before it is published.";
        }
        return new JdQualityResultVo(score, summary, issues, suggestions);
    }

    private static boolean containsAny(String text, String... parts) {
        if (text == null) return false;
        for (String part : parts) {
            if (part != null && text.contains(part.toLowerCase())) return true;
        }
        return false;
    }

    private static List<JdQualityIssueVo> parseIssues(Map<String, Object> root) {
        List<JdQualityIssueVo> out = new ArrayList<JdQualityIssueVo>();
        for (Map<String, Object> issue : AiJsonSupport.readObjectList(root, "issues")) {
            out.add(new JdQualityIssueVo(
                    AiJsonSupport.readString(issue, "dimension"),
                    AiJsonSupport.readString(issue, "severity"),
                    AiJsonSupport.readString(issue, "message")));
        }
        return out;
    }
}
