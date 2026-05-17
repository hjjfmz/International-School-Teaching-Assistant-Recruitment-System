package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class RecommendationNoteVo {
    private final String candidateId;
    private final String jobId;
    private final String candidateSourceHash;
    private final String jobSourceHash;
    private final String recommendTag;
    private final String recommendReason;
    private final List<String> recommendReasons;

    public RecommendationNoteVo(String candidateId, String jobId, String candidateSourceHash, String jobSourceHash,
            String recommendTag, String recommendReason, List<String> recommendReasons) {
        this.candidateId = candidateId == null ? "" : candidateId;
        this.jobId = jobId == null ? "" : jobId;
        this.candidateSourceHash = candidateSourceHash == null ? "" : candidateSourceHash;
        this.jobSourceHash = jobSourceHash == null ? "" : jobSourceHash;
        this.recommendTag = recommendTag == null ? "" : recommendTag.trim();
        this.recommendReason = recommendReason == null ? "" : recommendReason.trim();
        this.recommendReasons = recommendReasons == null ? new ArrayList<String>() : new ArrayList<String>(recommendReasons);
    }

    public String candidateId() { return candidateId; }

    public String jobId() { return jobId; }

    public String candidateSourceHash() { return candidateSourceHash; }

    public String jobSourceHash() { return jobSourceHash; }

    public String recommendTag() { return recommendTag; }

    public String recommendReason() { return recommendReason; }

    public List<String> recommendReasons() { return new ArrayList<String>(recommendReasons); }

    public Map<String, Object> toMap() {
        Map<String, Object> out = new LinkedHashMap<String, Object>();
        out.put("candidateId", candidateId);
        out.put("jobId", jobId);
        out.put("candidateSourceHash", candidateSourceHash);
        out.put("jobSourceHash", jobSourceHash);
        out.put("recommendTag", recommendTag);
        out.put("recommendReason", recommendReason);
        out.put("recommendReasons", new ArrayList<String>(recommendReasons));
        return out;
    }

    @SuppressWarnings("unchecked")
    public static RecommendationNoteVo fromMap(Map<String, Object> map) {
        if (map == null) return new RecommendationNoteVo("", "", "", "", "", "", null);
        List<String> reasons = new ArrayList<String>();
        Object reasonsObj = map.get("recommendReasons");
        if (reasonsObj instanceof List) {
            for (Object item : (List<Object>) reasonsObj) {
                if (item != null) reasons.add(String.valueOf(item));
            }
        }
        return new RecommendationNoteVo(
                asString(map.get("candidateId")),
                asString(map.get("jobId")),
                asString(map.get("candidateSourceHash")),
                asString(map.get("jobSourceHash")),
                asString(map.get("recommendTag")),
                asString(map.get("recommendReason")),
                reasons);
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
