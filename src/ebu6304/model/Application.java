package ebu6304.model;

public final class Application {
    public enum Status {
        SUBMITTED,
        ACCEPTED,
        REJECTED
    }

    private final String id;
    private final String applicantId;
    private final String jobId;
    private final Status status;
    private final long createdAt;
    private final int aiScore;

    public Application(String id, String applicantId, String jobId, Status status) {
        this(id, applicantId, jobId, status, System.currentTimeMillis(), -1);
    }

    public Application(String id, String applicantId, String jobId, Status status, long createdAt, int aiScore) {
        if (id == null) throw new IllegalArgumentException("id");
        if (applicantId == null) throw new IllegalArgumentException("applicantId");
        if (jobId == null) throw new IllegalArgumentException("jobId");
        if (status == null) throw new IllegalArgumentException("status");
        this.id = id;
        this.applicantId = applicantId;
        this.jobId = jobId;
        this.status = status;
        this.createdAt = createdAt;
        this.aiScore = aiScore;
    }

    public String id() { return id; }
    public String applicantId() { return applicantId; }
    public String jobId() { return jobId; }
    public Status status() { return status; }
    public long createdAt() { return createdAt; }
    public int aiScore() { return aiScore; }

    public Application withStatus(Status newStatus) {
        return new Application(id, applicantId, jobId, newStatus, createdAt, aiScore);
    }

    public Application withAiScore(int score) {
        return new Application(id, applicantId, jobId, status, createdAt, score);
    }
}
