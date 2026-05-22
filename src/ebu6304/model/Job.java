package ebu6304.model;

public final class Job {
    public enum Status {
        OPEN,
        CLOSED,
        COMPLETED
    }

    private final String id;
    private final String title;
    private final String description;
    private final String requiredSkills;
    private final int hoursPerWeek;
    private final String postedBy;
    private final Status status;
    private final String category;

    public Job(String id, String title, String description, String requiredSkills, int hoursPerWeek, String postedBy) {
        this(id, title, description, requiredSkills, hoursPerWeek, postedBy, Status.OPEN, "");
    }

    public Job(String id, String title, String description, String requiredSkills, int hoursPerWeek, String postedBy, Status status, String category) {
        if (id == null) throw new IllegalArgumentException("id");
        if (title == null) throw new IllegalArgumentException("title");
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.requiredSkills = requiredSkills == null ? "" : requiredSkills;
        this.hoursPerWeek = hoursPerWeek;
        this.postedBy = postedBy == null ? "" : postedBy;
        this.status = status == null ? Status.OPEN : status;
        this.category = category == null ? "" : category;
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public String requiredSkills() { return requiredSkills; }
    public int hoursPerWeek() { return hoursPerWeek; }
    public String postedBy() { return postedBy; }
    public Status status() { return status; }
    public String category() { return category; }

    public Job withStatus(Status newStatus) {
        return new Job(id, title, description, requiredSkills, hoursPerWeek, postedBy, newStatus, category);
    }

    public Job withCategory(String newCategory) {
        return new Job(id, title, description, requiredSkills, hoursPerWeek, postedBy, status, newCategory);
    }
}
