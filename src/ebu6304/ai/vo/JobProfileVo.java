package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class JobProfileVo {
    private final String jobId;
    private final String title;
    private final List<String> normalizedSkills;
    private final List<String> domainTags;
    private final SeniorityLevel seniorityLevel;
    private final int hoursPerWeek;
    private final List<String> keywords;
    private final String summary;

    public JobProfileVo(String jobId, String title, List<String> normalizedSkills, List<String> domainTags,
            SeniorityLevel seniorityLevel, int hoursPerWeek, List<String> keywords, String summary) {
        this.jobId = jobId == null ? "" : jobId;
        this.title = title == null ? "" : title.trim();
        this.normalizedSkills = normalizedSkills == null ? new ArrayList<String>() : new ArrayList<String>(normalizedSkills);
        this.domainTags = domainTags == null ? new ArrayList<String>() : new ArrayList<String>(domainTags);
        this.seniorityLevel = seniorityLevel == null ? SeniorityLevel.UNKNOWN : seniorityLevel;
        this.hoursPerWeek = hoursPerWeek < 0 ? 0 : hoursPerWeek;
        this.keywords = keywords == null ? new ArrayList<String>() : new ArrayList<String>(keywords);
        this.summary = summary == null ? "" : summary.trim();
    }

    public String jobId() { return jobId; }

    public String title() { return title; }

    public List<String> normalizedSkills() { return new ArrayList<String>(normalizedSkills); }

    public List<String> domainTags() { return new ArrayList<String>(domainTags); }

    public SeniorityLevel seniorityLevel() { return seniorityLevel; }

    public int hoursPerWeek() { return hoursPerWeek; }

    public List<String> keywords() { return new ArrayList<String>(keywords); }

    public String summary() { return summary; }

    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("jobId", jobId);
        out.put("title", title);
        out.put("normalizedSkills", new ArrayList<String>(normalizedSkills));
        out.put("domainTags", new ArrayList<String>(domainTags));
        out.put("seniorityLevel", seniorityLevel.name());
        out.put("hoursPerWeek", Integer.valueOf(hoursPerWeek));
        out.put("keywords", new ArrayList<String>(keywords));
        out.put("summary", summary);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static JobProfileVo fromMap(Map<String, Object> map) {
        if (map == null) return new JobProfileVo("", "", null, null, SeniorityLevel.UNKNOWN, 0, null, "");
        List<String> skills = new ArrayList<String>();
        Object skillsObj = map.get("normalizedSkills");
        if (skillsObj instanceof List) {
            for (Object item : (List<Object>) skillsObj) {
                if (item != null) skills.add(String.valueOf(item));
            }
        }
        List<String> domains = new ArrayList<String>();
        Object domainsObj = map.get("domainTags");
        if (domainsObj instanceof List) {
            for (Object item : (List<Object>) domainsObj) {
                if (item != null) domains.add(String.valueOf(item));
            }
        }
        List<String> keywords = new ArrayList<String>();
        Object keywordsObj = map.get("keywords");
        if (keywordsObj instanceof List) {
            for (Object item : (List<Object>) keywordsObj) {
                if (item != null) keywords.add(String.valueOf(item));
            }
        }
        SeniorityLevel level;
        try {
            level = SeniorityLevel.valueOf(String.valueOf(map.get("seniorityLevel")));
        } catch (RuntimeException ex) {
            level = SeniorityLevel.UNKNOWN;
        }
        int hours = 0;
        Object hoursObj = map.get("hoursPerWeek");
        if (hoursObj instanceof Number) hours = ((Number) hoursObj).intValue();
        return new JobProfileVo(
                asString(map.get("jobId")),
                asString(map.get("title")),
                skills,
                domains,
                level,
                hours,
                keywords,
                asString(map.get("summary")));
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
