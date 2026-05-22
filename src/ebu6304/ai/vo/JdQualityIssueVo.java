package ebu6304.ai.vo;

public final class JdQualityIssueVo {
    private final String dimension;
    private final String severity;
    private final String message;

    public JdQualityIssueVo(String dimension, String severity, String message) {
        this.dimension = dimension == null ? "" : dimension.trim();
        this.severity = severity == null ? "medium" : severity.trim().toLowerCase();
        this.message = message == null ? "" : message.trim();
    }

    public String dimension() { return dimension; }

    public String severity() { return severity; }

    public String message() { return message; }
}
