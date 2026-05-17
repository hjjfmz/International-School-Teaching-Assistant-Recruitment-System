package ebu6304.ai.client;

import java.io.IOException;

public interface AiChatClient {
    boolean isConfigured();

    String chat(AiPrompt prompt) throws IOException, InterruptedException;

    default String chatStream(AiPrompt prompt, AiStreamListener listener) throws IOException, InterruptedException {
        String full = chat(prompt);
        if (listener != null) {
            listener.onStart();
            if (full != null && !full.isEmpty()) listener.onDelta(full);
            listener.onComplete(full == null ? "" : full);
        }
        return full;
    }
}
