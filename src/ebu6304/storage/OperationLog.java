package ebu6304.storage;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;

public final class OperationLog {
    private OperationLog() {}

    public static synchronized void append(Path logFile, String level, String message) {
        if (logFile == null) return;
        if (level == null) level = "INFO";
        if (message == null) message = "";
        String line = LocalDateTime.now() + "\t" + level + "\t" + message + System.lineSeparator();
        try {
            if (Files.exists(logFile) && Files.isDirectory(logFile)) {
                System.err.println("[OperationLog] Log path is a directory, cannot append: " + logFile);
                return;
            }
            Path parent = logFile.getParent();
            if (parent != null) Files.createDirectories(parent);
            Files.write(logFile, line.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException ex) {
            System.err.println("[OperationLog] Failed to append log to: " + logFile);
            ex.printStackTrace(System.err);
        }
    }
}
