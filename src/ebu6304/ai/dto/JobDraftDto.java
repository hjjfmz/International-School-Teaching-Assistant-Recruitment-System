package ebu6304.ai.dto;

public final class JobDraftDto {
    private final String title;
    private final String description;
    private final String requiredSkills;
    private final int hoursPerWeek;

    public JobDraftDto(String title, String description, String requiredSkills, int hoursPerWeek) {
        this.title = title == null ? "" : title.trim();
        this.description = description == null ? "" : description.trim();
        this.requiredSkills = requiredSkills == null ? "" : requiredSkills.trim();
        this.hoursPerWeek = hoursPerWeek;
    }

    public String title() { return title; }

    public String description() { return description; }

    public String requiredSkills() { return requiredSkills; }

    public int hoursPerWeek() { return hoursPerWeek; }
}
