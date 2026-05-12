package ebu6304.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

public final class DeepSeekConfig {
    private final String apiKey;
    private final String baseUrl;
    private final Integer maxResumeChars;

    private DeepSeekConfig(String apiKey, String baseUrl, Integer maxResumeChars) {
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.baseUrl = baseUrl == null ? "" : baseUrl.trim();
        this.maxResumeChars = maxResumeChars;
    }

    public String apiKey() { return apiKey; }

    public String baseUrl() { return baseUrl; }

    public Integer maxResumeChars() { return maxResumeChars; }

    public static Path defaultConfigPath() {
        // Look for deepseek.properties in the current project root directory
        return Paths.get("deepseek.properties");
    }

    public static DeepSeekConfig loadDefault() {
        return load(defaultConfigPath());
    }

    public static DeepSeekConfig load(Path path) {
        if (path == null || !Files.exists(path) || Files.isDirectory(path)) {
            return new DeepSeekConfig("", "", null);
        }

        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
        } catch (IOException ex) {
            return new DeepSeekConfig("", "", null);
        }

        String apiKey = trimToEmpty(p.getProperty("api_key"));
        String baseUrl = trimToEmpty(p.getProperty("base_url"));
        Integer maxResumeChars = parseIntNullable(p.getProperty("max_resume_chars"));
        return new DeepSeekConfig(apiKey, baseUrl, maxResumeChars);
    }

    private static String trimToEmpty(String s) {
        return s == null ? "" : s.trim();
    }

    private static Integer parseIntNullable(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.isEmpty()) return null;
        try {
            return Integer.valueOf(Integer.parseInt(t));
        } catch (RuntimeException ex) {
            return null;
        }
    }
}
