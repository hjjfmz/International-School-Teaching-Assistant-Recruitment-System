package ebu6304.ai.dto;

public final class JobProfileSourceDto {
    private final String jobId;
    private final String title;
    private final String requiredSkillsText;
    private final String descriptionText;
    private final int hoursPerWeek;

    public JobProfileSourceDto(String jobId, String title, String requiredSkillsText, String descriptionText, int hoursPerWeek) {
        this.jobId = jobId == null ? "" : jobId;
        this.title = title == null ? "" : title;
        this.requiredSkillsText = requiredSkillsText == null ? "" : requiredSkillsText;
        this.descriptionText = descriptionText == null ? "" : descriptionText;
        this.hoursPerWeek = hoursPerWeek;
    }

    public String jobId() { return jobId; }

    public String title() { return title; }

    public String requiredSkillsText() { return requiredSkillsText; }

    public String descriptionText() { return descriptionText; }

    public int hoursPerWeek() { return hoursPerWeek; }
}
