package ebu6304.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public final class OperationLog {
    private OperationLog() {}

    public static void append(Path logFile, String level, String message) {
        if (logFile == null) return;
        if (level == null) level = "INFO";
        if (message == null) message = "";
        String line = LocalDateTime.now() + "\t" + level + "\t" + message + System.lineSeparator();
        try {
            Files.write(logFile, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        } catch (IOException ignored) {
        }
    }
}
