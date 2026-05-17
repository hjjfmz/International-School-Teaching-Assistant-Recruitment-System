package ebu6304.ai.client;

import java.nio.file.Path;

public interface AiClientFactory {
    AiChatClient createChatClient();

    boolean isConfigured();

    Path configPath();
}
