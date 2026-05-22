package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CandidateProfileVo {
    private final String candidateId;
    private final List<String> normalizedSkills;
    private final List<String> domainTags;
    private final SeniorityLevel seniorityLevel;
    private final int yearsExperience;
    private final String summary;

    public CandidateProfileVo(String candidateId, List<String> normalizedSkills, List<String> domainTags,
            SeniorityLevel seniorityLevel, int yearsExperience, String summary) {
        this.candidateId = candidateId == null ? "" : candidateId;
        this.normalizedSkills = normalizedSkills == null ? new ArrayList<String>() : new ArrayList<String>(normalizedSkills);
        this.domainTags = domainTags == null ? new ArrayList<String>() : new ArrayList<String>(domainTags);
        this.seniorityLevel = seniorityLevel == null ? SeniorityLevel.UNKNOWN : seniorityLevel;
        this.yearsExperience = yearsExperience < 0 ? 0 : yearsExperience;
        this.summary = summary == null ? "" : summary.trim();
    }

    public String candidateId() { return candidateId; }

    public List<String> normalizedSkills() { return new ArrayList<String>(normalizedSkills); }

    public List<String> domainTags() { return new ArrayList<String>(domainTags); }

    public SeniorityLevel seniorityLevel() { return seniorityLevel; }

    public int yearsExperience() { return yearsExperience; }

    public String summary() { return summary; }

    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("candidateId", candidateId);
        out.put("normalizedSkills", new ArrayList<String>(normalizedSkills));
        out.put("domainTags", new ArrayList<String>(domainTags));
        out.put("seniorityLevel", seniorityLevel.name());
        out.put("yearsExperience", Integer.valueOf(yearsExperience));
        out.put("summary", summary);
        return out;
    }

    @SuppressWarnings("unchecked")
    public static CandidateProfileVo fromMap(Map<String, Object> map) {
        if (map == null) return new CandidateProfileVo("", null, null, SeniorityLevel.UNKNOWN, 0, "");
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
        SeniorityLevel level;
        try {
            level = SeniorityLevel.valueOf(String.valueOf(map.get("seniorityLevel")));
        } catch (RuntimeException ex) {
            level = SeniorityLevel.UNKNOWN;
        }
        int years = 0;
        Object yearsObj = map.get("yearsExperience");
        if (yearsObj instanceof Number) years = ((Number) yearsObj).intValue();
        return new CandidateProfileVo(
                asString(map.get("candidateId")),
                skills,
                domains,
                level,
                years,
                asString(map.get("summary")));
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
