package ebu6304.ai.vo;

public enum SeniorityLevel {
    UNKNOWN(0),
    JUNIOR(1),
    MID(2),
    SENIOR(3),
    LEAD(4);

    private final int rank;

    SeniorityLevel(int rank) {
        this.rank = rank;
    }

    public int rank() {
        return rank;
    }

    public static SeniorityLevel fromText(String text) {
        String lower = text == null ? "" : text.trim().toLowerCase();
        if (lower.contains("lead") || lower.contains("principal") || lower.contains("manager")) return LEAD;
        if (lower.contains("senior") || lower.contains("experienced") || lower.contains("advanced")) return SENIOR;
        if (lower.contains("mid") || lower.contains("intermediate")) return MID;
        if (lower.contains("junior") || lower.contains("entry") || lower.contains("assistant") || lower.contains("intern")) return JUNIOR;
        return UNKNOWN;
    }
}
