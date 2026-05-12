package ebu6304.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ebu6304.model.Applicant;
import ebu6304.model.Job;
import ebu6304.storage.MiniJson;

public final class DeepSeekClient {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";
    private static final int DEFAULT_MAX_RESUME_CHARS = 6000;

    private final HttpClient http;
    private final String baseUrl;
    private final String apiKey;
    private final int maxResumeChars;

    public DeepSeekClient() {
        this(loadConfig());
    }

    public DeepSeekClient(String baseUrl, String apiKey) {
        this(loadConfig(), baseUrl, apiKey);
    }

    private DeepSeekClient(DeepSeekConfig cfg) {
        this(cfg, null, null);
    }

    private DeepSeekClient(DeepSeekConfig cfg, String envBaseUrl, String envApiKey) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String cfgBaseUrl = cfg == null ? "" : cfg.baseUrl();
        String cfgApiKey = cfg == null ? "" : cfg.apiKey();
        Integer cfgMax = cfg == null ? null : cfg.maxResumeChars();

        // precedence: config file -> env vars -> defaults
        String resolvedBaseUrl = firstNonBlank(cfgBaseUrl, envBaseUrl, System.getenv("DEEPSEEK_BASE_URL"), DEFAULT_BASE_URL);
        String resolvedApiKey = firstNonBlank(cfgApiKey, envApiKey, System.getenv("DEEPSEEK_API_KEY"), "");
        int resolvedMax = cfgMax != null ? cfgMax.intValue() : readIntEnv("DEEPSEEK_MAX_RESUME_CHARS", DEFAULT_MAX_RESUME_CHARS);

        this.baseUrl = resolvedBaseUrl;
        this.apiKey = resolvedApiKey;
        this.maxResumeChars = resolvedMax;
    }

    public boolean isConfigured() {
        return apiKey != null && !apiKey.isEmpty();
    }

    public static Path configPath() {
        String p = System.getenv("DEEPSEEK_CONFIG_PATH");
        if (p != null && !p.trim().isEmpty()) return Paths.get(p.trim());
        return DeepSeekConfig.defaultConfigPath();
    }

    private static DeepSeekConfig loadConfig() {
        return DeepSeekConfig.load(configPath());
    }

    public Map<String, AiScore> rankApplicantsForJob(Job job, List<Applicant> applicants) throws IOException, InterruptedException {
        if (job == null) throw new IllegalArgumentException("job");
        if (applicants == null) applicants = new ArrayList<Applicant>();
        if (!isConfigured()) throw new IOException("Missing DEEPSEEK_API_KEY");

        String prompt = buildPrompt(job, applicants);
        String body = buildRequestBody(prompt);

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("DeepSeek HTTP " + resp.statusCode() + ": " + resp.body());
        }

        String content = extractAssistantContent(resp.body());
        return parseScores(content);
    }

    private String buildPrompt(Job job, List<Applicant> applicants) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一位资深求职岗位匹配分析专家。请根据提供的岗位 JD 和候选人的【简历正文全文】，完成专业的岗位匹配度深度分析。\n");
        sb.append("约束:\n");
        sb.append("- 分析范围：涵盖技能要求、项目经验、实习经历、学历硬性条件、软素质要求五大维度。\n");
        sb.append("- 核心要求：必须深度挖掘候选人【简历正文内容】，严禁仅基于简短的技能标签做表面分析。\n");
        sb.append("- 输出格式要求：必须返回 STRICT JSON 格式，JSON Schema: {\"results\":[{\"id\":\"TA账号\",\"score\":综合匹配度百分比(0-100),\"reason\":\"### 维度匹配评分\n(1-10分打分并简述)\n\n### 优势亮点\n(基于简历正文提炼的三个核心优势)\n\n### 缺失短板\n(明确指出该候选人相对于 JD 缺失的背景)\"}]}\n\n");

        sb.append("### 招聘岗位 JD ###\n");
        sb.append("岗位名称: ").append(safe(job.title())).append("\n");
        sb.append("岗位描述: ").append(safe(job.description())).append("\n");
        sb.append("技能要求: ").append(safe(job.requiredSkills())).append("\n\n");

        sb.append("### 候选人简历详细内容 ###\n");
        for (Applicant a : applicants) {
            if (a == null) continue;
            String fullResume = safeReadResumeText(a);
            sb.append("--- [候选人 ID: ").append(safe(a.id())).append("] ---\n");
            sb.append("【基础简介】: ").append(safe(a.description())).append("\n");
            sb.append("【简历正文提取结果】: <<\n");
            sb.append(fullResume.isEmpty() ? "(未能提取到简历正文文本，请基于已知信息分析并提示简历内容缺失)" : safe(trimToMax(fullResume, maxResumeChars))).append("\n");
            sb.append(">>\n\n");
        }
        
        sb.append("请输出每位候选人的 0-100 综合评分及上述三个模块的专业理由。");
        return sb.toString();
    }

    private static String safeReadResumeText(Applicant a) {
        if (a == null) return "";
        String p = a.cvPath();
        if (p == null) return "";
        String s = p.trim();
        if (s.isEmpty()) return "";
        try {
            return ResumeTextExtractor.extract(Paths.get(s));
        } catch (Exception ex) {
            return "";
        }
    }

    private static String trimToMax(String s, int max) {
        if (s == null) return "";
        if (max <= 0) return "";
        if (s.length() <= max) return s;
        return s.substring(0, max);
    }

    private static int readIntEnv(String key, int def) {
        try {
            String v = System.getenv(key);
            if (v == null) return def;
            v = v.trim();
            if (v.isEmpty()) return def;
            return Integer.parseInt(v);
        } catch (RuntimeException ex) {
            return def;
        }
    }

    private static String firstNonBlank(String a, String b, String c, String d) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null && !b.trim().isEmpty()) return b.trim();
        if (c != null && !c.trim().isEmpty()) return c.trim();
        if (d != null && !d.trim().isEmpty()) return d.trim();
        return "";
    }

    private static String buildRequestBody(String prompt) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("model", "deepseek-v4-flash");

        List<Object> messages = new ArrayList<Object>();
        Map<String, Object> sys = new LinkedHashMap<String, Object>();
        sys.put("role", "system");
        sys.put("content", "You are a helpful assistant.");
        messages.add(sys);

        Map<String, Object> user = new LinkedHashMap<String, Object>();
        user.put("role", "user");
        user.put("content", prompt);
        messages.add(user);

        root.put("messages", messages);
        root.put("temperature", Double.valueOf(0.2));
        return MiniJson.stringify(root);
    }

    @SuppressWarnings("unchecked")
    private static String extractAssistantContent(String responseJson) throws IOException {
        try {
            Object parsed = MiniJson.parse(responseJson);
            if (!(parsed instanceof Map)) return responseJson;
            Map<String, Object> root = (Map<String, Object>) parsed;
            Object choicesObj = root.get("choices");
            if (!(choicesObj instanceof List)) return responseJson;
            List<Object> choices = (List<Object>) choicesObj;
            if (choices.isEmpty()) return "";
            Object first = choices.get(0);
            if (!(first instanceof Map)) return responseJson;
            Map<String, Object> choice = (Map<String, Object>) first;
            Object msgObj = choice.get("message");
            if (!(msgObj instanceof Map)) return responseJson;
            Map<String, Object> msg = (Map<String, Object>) msgObj;
            Object content = msg.get("content");
            return content == null ? "" : String.valueOf(content);
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse DeepSeek response: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, AiScore> parseScores(String content) throws IOException {
        try {
            Object parsed = MiniJson.parse(content);
            if (!(parsed instanceof Map)) throw new IOException("AI response is not JSON object");
            Map<String, Object> root = (Map<String, Object>) parsed;
            Object resultsObj = root.get("results");
            if (!(resultsObj instanceof List)) throw new IOException("AI response missing results[]");

            Map<String, AiScore> out = new LinkedHashMap<String, AiScore>();
            for (Object o : (List<Object>) resultsObj) {
                if (!(o instanceof Map)) continue;
                Map<String, Object> r = (Map<String, Object>) o;
                String id = r.get("id") == null ? "" : String.valueOf(r.get("id"));
                int score = toInt(r.get("score"));
                String reason = r.get("reason") == null ? "" : String.valueOf(r.get("reason"));
                if (id == null || id.trim().isEmpty()) continue;
                out.put(id.trim(), new AiScore(score, reason));
            }
            return out;
        } catch (RuntimeException ex) {
            throw new IOException("Failed to parse AI JSON content: " + ex.getMessage() + ", content=" + content);
        }
    }

    private static int toInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    public static final class AiScore {
        private final int score;
        private final String reason;

        public AiScore(int score, String reason) {
            this.score = clamp(score);
            this.reason = reason == null ? "" : reason;
        }

        public int score() { return score; }

        public String reason() { return reason; }

        private static int clamp(int v) {
            if (v < 0) return 0;
            if (v > 100) return 100;
            return v;
        }
    }
}
