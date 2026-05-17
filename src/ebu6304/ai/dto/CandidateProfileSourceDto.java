package ebu6304.ai.dto;

public final class CandidateProfileSourceDto {
    private final String candidateId;
    private final String skillsText;
    private final String summaryText;
    private final String resumeText;

    public CandidateProfileSourceDto(String candidateId, String skillsText, String summaryText, String resumeText) {
        this.candidateId = candidateId == null ? "" : candidateId;
        this.skillsText = skillsText == null ? "" : skillsText;
        this.summaryText = summaryText == null ? "" : summaryText;
        this.resumeText = resumeText == null ? "" : resumeText;
    }

    public String candidateId() { return candidateId; }

    public String skillsText() { return skillsText; }

    public String summaryText() { return summaryText; }

    public String resumeText() { return resumeText; }
}
