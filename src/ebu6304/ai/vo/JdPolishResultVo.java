package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.List;

public final class JdPolishResultVo {
    private final String title;
    private final String requiredSkills;
    private final String description;
    private final List<String> changeSummary;

    public JdPolishResultVo(String title, String requiredSkills, String description, List<String> changeSummary) {
        this.title = title == null ? "" : title.trim();
        this.requiredSkills = requiredSkills == null ? "" : requiredSkills.trim();
        this.description = description == null ? "" : description.trim();
        this.changeSummary = changeSummary == null ? new ArrayList<String>() : new ArrayList<String>(changeSummary);
    }

    public String title() { return title; }

    public String requiredSkills() { return requiredSkills; }

    public String description() { return description; }

    public List<String> changeSummary() { return new ArrayList<String>(changeSummary); }
}
