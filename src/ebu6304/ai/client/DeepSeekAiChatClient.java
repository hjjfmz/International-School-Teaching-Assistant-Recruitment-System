package ebu6304.ai.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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

import ebu6304.ai.DeepSeekConfig;
import ebu6304.storage.MiniJson;

public final class DeepSeekAiChatClient implements AiChatClient {
    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com";

    private final HttpClient http;
    private final String baseUrl;
    private final String apiKey;

    public DeepSeekAiChatClient() {
        this(loadConfig());
    }

    private DeepSeekAiChatClient(DeepSeekConfig cfg) {
        this.http = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        String cfgBaseUrl = cfg == null ? "" : cfg.baseUrl();
        String cfgApiKey = cfg == null ? "" : cfg.apiKey();
        this.baseUrl = firstNonBlank(cfgBaseUrl, System.getenv("DEEPSEEK_BASE_URL"), DEFAULT_BASE_URL);
        this.apiKey = firstNonBlank(cfgApiKey, System.getenv("DEEPSEEK_API_KEY"), "");
    }

    public static Path configPath() {
        String custom = System.getenv("DEEPSEEK_CONFIG_PATH");
        if (custom != null && !custom.trim().isEmpty()) {
            return Paths.get(custom.trim());
        }
        return DeepSeekConfig.defaultConfigPath();
    }

    private static DeepSeekConfig loadConfig() {
        return DeepSeekConfig.load(configPath());
    }

    @Override
    public boolean isConfigured() {
        return apiKey != null && !apiKey.trim().isEmpty();
    }

    @Override
    public String chat(AiPrompt prompt) throws IOException, InterruptedException {
        if (prompt == null) throw new IllegalArgumentException("prompt");
        if (!isConfigured()) throw new IOException("Missing DEEPSEEK_API_KEY");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(prompt.timeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt), StandardCharsets.UTF_8))
                .build();

        HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("DeepSeek HTTP " + resp.statusCode() + ": " + resp.body());
        }
        return AiJsonSupport.extractAssistantContent(resp.body());
    }

    @Override
    public String chatStream(AiPrompt prompt, AiStreamListener listener) throws IOException, InterruptedException {
        if (prompt == null) throw new IllegalArgumentException("prompt");
        if (!isConfigured()) throw new IOException("Missing DEEPSEEK_API_KEY");

        HttpRequest req = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .timeout(Duration.ofSeconds(prompt.timeoutSeconds()))
                .header("Content-Type", "application/json")
                .header("Accept", "text/event-stream")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(buildRequestBody(prompt, true), StandardCharsets.UTF_8))
                .build();

        HttpResponse<InputStream> resp = http.send(req, HttpResponse.BodyHandlers.ofInputStream());
        if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
            throw new IOException("DeepSeek HTTP " + resp.statusCode() + ": " + readAll(resp.body()));
        }

        StringBuilder full = new StringBuilder();
        if (listener != null) listener.onStart();

        BufferedReader reader = new BufferedReader(new InputStreamReader(resp.body(), StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.isEmpty() || !line.startsWith("data:")) continue;
            String data = line.substring(5).trim();
            if (data.isEmpty() || "[DONE]".equals(data)) continue;
            String delta = AiJsonSupport.extractStreamDelta(data);
            if (delta.isEmpty()) continue;
            full.append(delta);
            if (listener != null) listener.onDelta(delta);
        }
        if (listener != null) listener.onComplete(full.toString());
        return full.toString();
    }

    private static String buildRequestBody(AiPrompt prompt, boolean stream) {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("model", "deepseek-v4-flash");
        root.put("temperature", Double.valueOf(0.2));

        List<Object> messages = new ArrayList<Object>();
        Map<String, Object> sys = new LinkedHashMap<String, Object>();
        sys.put("role", "system");
        sys.put("content", prompt.systemPrompt());
        messages.add(sys);

        Map<String, Object> user = new LinkedHashMap<String, Object>();
        user.put("role", "user");
        user.put("content", prompt.userPrompt());
        messages.add(user);

        root.put("messages", messages);
        if (stream) root.put("stream", Boolean.TRUE);
        return MiniJson.stringify(root);
    }

    private static String buildRequestBody(AiPrompt prompt) {
        return buildRequestBody(prompt, false);
    }

    private static String readAll(InputStream in) throws IOException {
        if (in == null) return "";
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append('\n');
        }
        return sb.toString();
    }

    private static String firstNonBlank(String a, String b, String c) {
        if (a != null && !a.trim().isEmpty()) return a.trim();
        if (b != null && !b.trim().isEmpty()) return b.trim();
        if (c != null && !c.trim().isEmpty()) return c.trim();
        return "";
    }
}
