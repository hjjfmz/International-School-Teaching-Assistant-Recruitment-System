package ebu6304.ai.vo;

import java.util.ArrayList;
import java.util.List;

public final class JdQualityResultVo {
    private final int overallScore;
    private final String summary;
    private final List<JdQualityIssueVo> issues;
    private final List<String> suggestions;

    public JdQualityResultVo(int overallScore, String summary, List<JdQualityIssueVo> issues, List<String> suggestions) {
        this.overallScore = overallScore < 0 ? 0 : Math.min(100, overallScore);
        this.summary = summary == null ? "" : summary.trim();
        this.issues = issues == null ? new ArrayList<JdQualityIssueVo>() : new ArrayList<JdQualityIssueVo>(issues);
        this.suggestions = suggestions == null ? new ArrayList<String>() : new ArrayList<String>(suggestions);
    }

    public int overallScore() { return overallScore; }

    public String summary() { return summary; }

    public List<JdQualityIssueVo> issues() { return new ArrayList<JdQualityIssueVo>(issues); }

    public List<String> suggestions() { return new ArrayList<String>(suggestions); }
}
