package ebu6304.ai.client;

public interface AiStreamListener {
    default void onStart() {}

    void onDelta(String delta);

    default void onComplete(String fullText) {}
}
