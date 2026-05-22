package ebu6304.ai.client;

public final class AiPrompt {
    private final String systemPrompt;
    private final String userPrompt;
    private final int timeoutSeconds;

    public AiPrompt(String systemPrompt, String userPrompt, int timeoutSeconds) {
        this.systemPrompt = systemPrompt == null ? "" : systemPrompt.trim();
        this.userPrompt = userPrompt == null ? "" : userPrompt;
        this.timeoutSeconds = timeoutSeconds <= 0 ? 60 : timeoutSeconds;
    }

    public String systemPrompt() { return systemPrompt; }

    public String userPrompt() { return userPrompt; }

    public int timeoutSeconds() { return timeoutSeconds; }
}
