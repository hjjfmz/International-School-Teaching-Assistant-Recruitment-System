package ebu6304.model;

public final class Job {
    private final String id;
    private final String title;
    private final String description;
    private final String requiredSkills;
    private final int hoursPerWeek;
    private final String postedBy;

    public Job(String id, String title, String description, String requiredSkills, int hoursPerWeek, String postedBy) {
        if (id == null) throw new IllegalArgumentException("id");
        if (title == null) throw new IllegalArgumentException("title");
        this.id = id;
        this.title = title;
        this.description = description == null ? "" : description;
        this.requiredSkills = requiredSkills == null ? "" : requiredSkills;
        this.hoursPerWeek = hoursPerWeek;
        this.postedBy = postedBy == null ? "" : postedBy;
    }

    public String id() { return id; }
    public String title() { return title; }
    public String description() { return description; }
    public String requiredSkills() { return requiredSkills; }
    public int hoursPerWeek() { return hoursPerWeek; }
    public String postedBy() { return postedBy; }
}
