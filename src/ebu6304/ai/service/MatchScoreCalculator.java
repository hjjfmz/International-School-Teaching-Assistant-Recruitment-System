package ebu6304.ai.service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.ai.vo.JobProfileVo;

public final class MatchScoreCalculator {
    public JobMatchResultVo calculate(CandidateProfileVo candidate, JobProfileVo job) {
        List<String> matchedSkills = intersect(candidate == null ? null : candidate.normalizedSkills(), job == null ? null : job.normalizedSkills());
        List<String> missingSkills = difference(job == null ? null : job.normalizedSkills(), candidate == null ? null : candidate.normalizedSkills());

        int skillScore = calculateSkillScore(job == null ? null : job.normalizedSkills(), matchedSkills);
        int seniorityScore = calculateSeniorityScore(candidate, job);
        int domainScore = calculateDomainScore(candidate, job);

        int overall = (int) Math.round(skillScore * 0.6 + seniorityScore * 0.2 + domainScore * 0.2);
        if (!missingSkills.isEmpty() && missingSkills.size() <= 1) overall += 4;
        if (missingSkills.isEmpty()) overall += 6;
        if (overall > 100) overall = 100;

        return new JobMatchResultVo(
                candidate == null ? "" : candidate.candidateId(),
                job == null ? "" : job.jobId(),
                overall,
                skillScore,
                seniorityScore,
                domainScore,
                matchedSkills,
                missingSkills,
                null,
                "",
                "");
    }

    private static int calculateSkillScore(List<String> requiredSkills, List<String> matchedSkills) {
        if (requiredSkills == null || requiredSkills.isEmpty()) return 100;
        int matched = matchedSkills == null ? 0 : matchedSkills.size();
        return clamp((int) Math.round((matched * 100.0) / requiredSkills.size()));
    }

    private static int calculateSeniorityScore(CandidateProfileVo candidate, JobProfileVo job) {
        if (job == null || job.seniorityLevel().rank() == 0) return 100;
        if (candidate == null || candidate.seniorityLevel().rank() == 0) return 55;
        int diff = candidate.seniorityLevel().rank() - job.seniorityLevel().rank();
        if (diff >= 0) return 100;
        if (diff == -1) return 65;
        return 35;
    }

    private static int calculateDomainScore(CandidateProfileVo candidate, JobProfileVo job) {
        List<String> jobDomains = job == null ? null : job.domainTags();
        if (jobDomains == null || jobDomains.isEmpty()) return 100;
        List<String> candidateDomains = candidate == null ? null : candidate.domainTags();
        if (candidateDomains == null || candidateDomains.isEmpty()) return 40;
        int matched = intersect(candidateDomains, jobDomains).size();
        return clamp((int) Math.round((matched * 100.0) / jobDomains.size()));
    }

    private static List<String> intersect(List<String> left, List<String> right) {
        Set<String> out = new LinkedHashSet<String>();
        if (left == null || right == null) return new ArrayList<String>(out);
        for (String item : left) {
            if (item == null || item.trim().isEmpty()) continue;
            if (containsIgnoreCase(right, item)) out.add(item);
        }
        return new ArrayList<String>(out);
    }

    private static List<String> difference(List<String> required, List<String> actual) {
        List<String> out = new ArrayList<String>();
        if (required == null) return out;
        for (String item : required) {
            if (item == null || item.trim().isEmpty()) continue;
            if (!containsIgnoreCase(actual, item)) out.add(item);
        }
        return out;
    }

    private static boolean containsIgnoreCase(List<String> list, String value) {
        if (list == null || value == null) return false;
        for (String item : list) {
            if (item != null && item.equalsIgnoreCase(value)) return true;
        }
        return false;
    }

    private static int clamp(int score) {
        if (score < 0) return 0;
        if (score > 100) return 100;
        return score;
    }
}
