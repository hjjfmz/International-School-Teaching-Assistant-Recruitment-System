package ebu6304.ai.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ebu6304.ai.vo.SeniorityLevel;

public final class AiTextUtils {
    private static final Pattern YEARS_PATTERN = Pattern.compile("(\\d{1,2})\\s*\\+?\\s*(?:years?|yrs?)", Pattern.CASE_INSENSITIVE);

    private static final Map<String, String[]> DOMAIN_KEYWORDS = new LinkedHashMap<String, String[]>();

    static {
        DOMAIN_KEYWORDS.put("teaching-support", new String[] { "teaching", "tutor", "tutorial", "module", "lab", "grading", "marking", "student", "education", "feedback" });
        DOMAIN_KEYWORDS.put("software-development", new String[] { "java", "python", "git", "software", "programming", "coding", "debugging", "agile" });
        DOMAIN_KEYWORDS.put("databases", new String[] { "sql", "database", "mysql", "postgres", "query", "data model" });
        DOMAIN_KEYWORDS.put("exam-operations", new String[] { "invigilation", "exam", "assessment", "attendance", "detail" });
        DOMAIN_KEYWORDS.put("data-analysis", new String[] { "excel", "analysis", "analytics", "reporting", "statistics", "power bi" });
        DOMAIN_KEYWORDS.put("communication", new String[] { "communication", "presentation", "english", "writing", "stakeholder" });
        DOMAIN_KEYWORDS.put("admin-support", new String[] { "organizing", "administration", "scheduling", "coordination", "support desk" });
    }

    private AiTextUtils() {}

    public static List<String> extractSkills(String... texts) {
        Set<String> out = new LinkedHashSet<String>();
        for (String text : texts) {
            if (text == null || text.trim().isEmpty()) continue;
            for (String token : splitTokens(text)) {
                String normalized = normalizeSkill(token);
                if (!normalized.isEmpty()) out.add(normalized);
            }
        }
        return new ArrayList<String>(out);
    }

    public static List<String> detectDomains(String... texts) {
        Set<String> out = new LinkedHashSet<String>();
        String joined = joinTexts(texts).toLowerCase();
        for (Map.Entry<String, String[]> entry : DOMAIN_KEYWORDS.entrySet()) {
            for (String keyword : entry.getValue()) {
                if (joined.contains(keyword.toLowerCase())) {
                    out.add(entry.getKey());
                    break;
                }
            }
        }
        return new ArrayList<String>(out);
    }

    public static SeniorityLevel detectSeniority(String... texts) {
        SeniorityLevel best = SeniorityLevel.UNKNOWN;
        for (String text : texts) {
            SeniorityLevel current = SeniorityLevel.fromText(text);
            if (current.rank() > best.rank()) best = current;
        }
        return best;
    }

    public static int detectYearsExperience(String... texts) {
        int best = 0;
        for (String text : texts) {
            if (text == null) continue;
            Matcher matcher = YEARS_PATTERN.matcher(text);
            while (matcher.find()) {
                try {
                    best = Math.max(best, Integer.parseInt(matcher.group(1)));
                } catch (RuntimeException ignored) {
                }
            }
        }
        return best;
    }

    public static List<String> mergeUnique(List<String> first, List<String> second) {
        Set<String> out = new LinkedHashSet<String>();
        if (first != null) out.addAll(first);
        if (second != null) out.addAll(second);
        return new ArrayList<String>(out);
    }

    public static String normalizeWhitespace(String text) {
        if (text == null) return "";
        return text.replace("\r\n", "\n").replace('\r', '\n').replaceAll("[ \t]+", " ").replaceAll("\n{3,}", "\n\n").trim();
    }

    public static String shorten(String text, int maxChars) {
        String normalized = normalizeWhitespace(text);
        if (maxChars <= 0 || normalized.length() <= maxChars) return normalized;
        return normalized.substring(0, maxChars);
    }

    public static String sourceHash(String... texts) {
        return Integer.toHexString(joinTexts(texts).hashCode());
    }

    private static String joinTexts(String... texts) {
        StringBuilder sb = new StringBuilder();
        if (texts != null) {
            for (String text : texts) {
                if (text == null || text.trim().isEmpty()) continue;
                if (sb.length() > 0) sb.append('\n');
                sb.append(text.trim());
            }
        }
        return sb.toString();
    }

    private static List<String> splitTokens(String text) {
        String normalized = normalizeWhitespace(text)
                .replace('/', ',')
                .replace('|', ',')
                .replace(';', ',')
                .replace('，', ',')
                .replace('；', ',')
                .replace('、', ',');
        String[] parts = normalized.split("[,\\n]");
        List<String> out = new ArrayList<String>();
        for (String part : parts) {
            String token = part == null ? "" : part.trim();
            if (token.isEmpty()) continue;
            if (token.length() > 40 && token.contains(" ")) {
                out.addAll(Arrays.asList(token.split("\\s{2,}")));
            } else {
                out.add(token);
            }
        }
        return out;
    }

    public static String normalizeSkill(String token) {
        String value = token == null ? "" : token.trim().toLowerCase();
        if (value.isEmpty()) return "";
        value = value.replaceAll("[^a-z0-9+#.\\- ]", " ").replaceAll("\\s+", " ").trim();
        if (value.isEmpty()) return "";
        if ("js".equals(value)) return "javascript";
        if ("py".equals(value) || "python3".equals(value)) return "python";
        if ("ms excel".equals(value) || "microsoft excel".equals(value)) return "excel";
        if ("mysql database".equals(value)) return "mysql";
        if ("postgresql".equals(value)) return "postgres";
        if ("communication skills".equals(value)) return "communication";
        if ("problem solving".equals(value)) return "problem-solving";
        if ("team work".equals(value)) return "teamwork";
        return value;
    }
}
