package ebu6304.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for AI-powered skill matching between job requirements and applicant skills.
 * This class provides methods to calculate match percentage and identify missing skills.
 */
public final class SkillMatcher {

    /**
     * Calculate the match percentage between required skills and applicant skills.
     * 
     * @param requiredSkills Comma-separated string of required skills
     * @param applicantSkills Comma-separated string of applicant skills
     * @return Match percentage (0-100)
     */
    public static int calculateMatchPercentage(String requiredSkills, String applicantSkills) {
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return 100; // No requirements means 100% match
        }
        if (applicantSkills == null || applicantSkills.trim().isEmpty()) {
            return 0; // No skills means 0% match
        }

        Set<String> required = parseSkills(requiredSkills);
        Set<String> applicant = parseSkills(applicantSkills);

        if (required.isEmpty()) {
            return 100;
        }

        // Count matching skills (case-insensitive)
        int matchCount = 0;
        for (String skill : applicant) {
            if (containsIgnoreCase(required, skill)) {
                matchCount++;
            }
        }

        return (int) ((matchCount * 100.0) / required.size());
    }

    /**
     * Get the list of missing skills that the applicant doesn't have.
     * 
     * @param requiredSkills Comma-separated string of required skills
     * @param applicantSkills Comma-separated string of applicant skills
     * @return List of missing skill names
     */
    public static List<String> getMissingSkills(String requiredSkills, String applicantSkills) {
        List<String> missing = new ArrayList<String>();
        
        if (requiredSkills == null || requiredSkills.trim().isEmpty()) {
            return missing;
        }

        Set<String> required = parseSkills(requiredSkills);
        Set<String> applicant = parseSkills(applicantSkills);

        for (String skill : required) {
            if (!containsIgnoreCase(applicant, skill)) {
                missing.add(skill);
            }
        }

        return missing;
    }

    /**
     * Get the list of matching skills that the applicant has.
     * 
     * @param requiredSkills Comma-separated string of required skills
     * @param applicantSkills Comma-separated string of applicant skills
     * @return List of matching skill names
     */
    public static List<String> getMatchingSkills(String requiredSkills, String applicantSkills) {
        List<String> matching = new ArrayList<String>();
        
        if (requiredSkills == null || applicantSkills == null) {
            return matching;
        }

        Set<String> required = parseSkills(requiredSkills);
        Set<String> applicant = parseSkills(applicantSkills);

        for (String skill : applicant) {
            if (containsIgnoreCase(required, skill)) {
                matching.add(skill);
            }
        }

        return matching;
    }

    /**
     * Get a formatted string representation of match result.
     * 
     * @param requiredSkills Comma-separated string of required skills
     * @param applicantSkills Comma-separated string of applicant skills
     * @return Formatted match result string
     */
    public static String getMatchSummary(String requiredSkills, String applicantSkills) {
        int percentage = calculateMatchPercentage(requiredSkills, applicantSkills);
        List<String> missing = getMissingSkills(requiredSkills, applicantSkills);
        
        StringBuilder sb = new StringBuilder();
        sb.append(percentage).append("% match");
        
        if (!missing.isEmpty()) {
            sb.append(" | Missing: ").append(String.join(", ", missing));
        }
        
        return sb.toString();
    }

    /**
     * Parse comma-separated skills into a set of normalized skill names.
     */
    private static Set<String> parseSkills(String skills) {
        Set<String> result = new HashSet<String>();
        if (skills == null || skills.trim().isEmpty()) {
            return result;
        }

        String[] parts = skills.split(",");
        for (String part : parts) {
            String normalized = part.trim().toLowerCase();
            if (!normalized.isEmpty()) {
                result.add(normalized);
            }
        }
        return result;
    }

    /**
     * Check if a set contains a string (case-insensitive).
     */
    private static boolean containsIgnoreCase(Set<String> set, String value) {
        if (set == null || value == null) {
            return false;
        }
        String lowerValue = value.toLowerCase();
        for (String s : set) {
            if (s.equalsIgnoreCase(lowerValue)) {
                return true;
            }
        }
        return false;
    }
}