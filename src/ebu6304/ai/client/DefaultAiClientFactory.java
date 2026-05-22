package ebu6304.ai.client;

import java.nio.file.Path;

public final class DefaultAiClientFactory implements AiClientFactory {
    @Override
    public AiChatClient createChatClient() {
        return new DeepSeekAiChatClient();
    }

    @Override
    public boolean isConfigured() {
        return createChatClient().isConfigured();
    }

    @Override
    public Path configPath() {
        return DeepSeekAiChatClient.configPath();
    }
}
