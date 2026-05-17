package ebu6304.ai.repository;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.ai.vo.JobProfileVo;
import ebu6304.ai.vo.RecommendationNoteVo;
import ebu6304.storage.MiniJson;

public final class AiDatasetRepository {
    public static final class CandidateCacheEntry {
        private final String sourceHash;
        private final long updatedAt;
        private final CandidateProfileVo profile;

        CandidateCacheEntry(String sourceHash, long updatedAt, CandidateProfileVo profile) {
            this.sourceHash = sourceHash == null ? "" : sourceHash;
            this.updatedAt = updatedAt;
            this.profile = profile;
        }

        public String sourceHash() { return sourceHash; }

        public long updatedAt() { return updatedAt; }

        public CandidateProfileVo profile() { return profile; }
    }

    public static final class JobCacheEntry {
        private final String sourceHash;
        private final long updatedAt;
        private final JobProfileVo profile;

        JobCacheEntry(String sourceHash, long updatedAt, JobProfileVo profile) {
            this.sourceHash = sourceHash == null ? "" : sourceHash;
            this.updatedAt = updatedAt;
            this.profile = profile;
        }

        public String sourceHash() { return sourceHash; }

        public long updatedAt() { return updatedAt; }

        public JobProfileVo profile() { return profile; }
    }

    private final Path datasetFile;
    private final Map<String, CandidateCacheEntry> candidateProfiles = new LinkedHashMap<String, CandidateCacheEntry>();
    private final Map<String, JobCacheEntry> jobProfiles = new LinkedHashMap<String, JobCacheEntry>();
    private final Map<String, RecommendationNoteVo> recommendationNotes = new LinkedHashMap<String, RecommendationNoteVo>();

    public AiDatasetRepository(Path datasetFile) {
        this.datasetFile = datasetFile;
        load();
    }

    public synchronized CandidateCacheEntry getCandidateProfile(String candidateId) {
        return candidateProfiles.get(candidateId == null ? "" : candidateId);
    }

    public synchronized void putCandidateProfile(String candidateId, String sourceHash, CandidateProfileVo profile) {
        candidateProfiles.put(candidateId == null ? "" : candidateId,
                new CandidateCacheEntry(sourceHash, System.currentTimeMillis(), profile));
        persist();
    }

    public synchronized JobCacheEntry getJobProfile(String jobId) {
        return jobProfiles.get(jobId == null ? "" : jobId);
    }

    public synchronized void putJobProfile(String jobId, String sourceHash, JobProfileVo profile) {
        jobProfiles.put(jobId == null ? "" : jobId,
                new JobCacheEntry(sourceHash, System.currentTimeMillis(), profile));
        persist();
    }

    public synchronized RecommendationNoteVo getRecommendationNote(String candidateId, String jobId, String candidateHash, String jobHash) {
        RecommendationNoteVo note = recommendationNotes.get(key(candidateId, jobId));
        if (note == null) return null;
        if (!safeEquals(candidateHash, note.candidateSourceHash())) return null;
        if (!safeEquals(jobHash, note.jobSourceHash())) return null;
        return note;
    }

    public synchronized void putRecommendationNote(RecommendationNoteVo note) {
        if (note == null) return;
        recommendationNotes.put(key(note.candidateId(), note.jobId()), note);
        persist();
    }

    private void load() {
        candidateProfiles.clear();
        jobProfiles.clear();
        recommendationNotes.clear();
        if (datasetFile == null || !Files.exists(datasetFile)) return;
        try {
            String json = new String(Files.readAllBytes(datasetFile), StandardCharsets.UTF_8);
            Object parsed = MiniJson.parse(json);
            if (!(parsed instanceof Map)) return;
            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) parsed;
            loadCandidates(root.get("candidateProfiles"));
            loadJobs(root.get("jobProfiles"));
            loadNotes(root.get("recommendationNotes"));
        } catch (Exception ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private void loadCandidates(Object value) {
        if (!(value instanceof List)) return;
        for (Object item : (List<Object>) value) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            String candidateId = asString(map.get("candidateId"));
            String sourceHash = asString(map.get("sourceHash"));
            long updatedAt = asLong(map.get("updatedAt"));
            Object profileObj = map.get("profile");
            if (!(profileObj instanceof Map) || candidateId.isEmpty()) continue;
            CandidateProfileVo profile = CandidateProfileVo.fromMap(castMap(profileObj));
            candidateProfiles.put(candidateId, new CandidateCacheEntry(sourceHash, updatedAt, profile));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadJobs(Object value) {
        if (!(value instanceof List)) return;
        for (Object item : (List<Object>) value) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            String jobId = asString(map.get("jobId"));
            String sourceHash = asString(map.get("sourceHash"));
            long updatedAt = asLong(map.get("updatedAt"));
            Object profileObj = map.get("profile");
            if (!(profileObj instanceof Map) || jobId.isEmpty()) continue;
            JobProfileVo profile = JobProfileVo.fromMap(castMap(profileObj));
            jobProfiles.put(jobId, new JobCacheEntry(sourceHash, updatedAt, profile));
        }
    }

    @SuppressWarnings("unchecked")
    private void loadNotes(Object value) {
        if (!(value instanceof List)) return;
        for (Object item : (List<Object>) value) {
            if (!(item instanceof Map)) continue;
            RecommendationNoteVo note = RecommendationNoteVo.fromMap(castMap(item));
            recommendationNotes.put(key(note.candidateId(), note.jobId()), note);
        }
    }

    private void persist() {
        if (datasetFile == null) return;
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        List<Object> candidates = new ArrayList<Object>();
        for (Map.Entry<String, CandidateCacheEntry> entry : candidateProfiles.entrySet()) {
            Map<String, Object> out = new LinkedHashMap<String, Object>();
            out.put("candidateId", entry.getKey());
            out.put("sourceHash", entry.getValue().sourceHash());
            out.put("updatedAt", Long.valueOf(entry.getValue().updatedAt()));
            out.put("profile", entry.getValue().profile() == null ? new LinkedHashMap<String, Object>() : entry.getValue().profile().toMap());
            candidates.add(out);
        }
        root.put("candidateProfiles", candidates);

        List<Object> jobs = new ArrayList<Object>();
        for (Map.Entry<String, JobCacheEntry> entry : jobProfiles.entrySet()) {
            Map<String, Object> out = new LinkedHashMap<String, Object>();
            out.put("jobId", entry.getKey());
            out.put("sourceHash", entry.getValue().sourceHash());
            out.put("updatedAt", Long.valueOf(entry.getValue().updatedAt()));
            out.put("profile", entry.getValue().profile() == null ? new LinkedHashMap<String, Object>() : entry.getValue().profile().toMap());
            jobs.add(out);
        }
        root.put("jobProfiles", jobs);

        List<Object> notes = new ArrayList<Object>();
        for (RecommendationNoteVo note : recommendationNotes.values()) {
            notes.add(note.toMap());
        }
        root.put("recommendationNotes", notes);

        try {
            if (datasetFile.getParent() != null) Files.createDirectories(datasetFile.getParent());
            Files.write(datasetFile, (MiniJson.stringifyPretty(root) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException ignored) {
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> castMap(Object value) {
        return new LinkedHashMap<String, Object>((Map<String, Object>) value);
    }

    private static String key(String candidateId, String jobId) {
        return (candidateId == null ? "" : candidateId) + "::" + (jobId == null ? "" : jobId);
    }

    private static boolean safeEquals(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }

    private static String asString(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static long asLong(Object value) {
        if (value instanceof Number) return ((Number) value).longValue();
        if (value == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (RuntimeException ex) {
            return 0L;
        }
    }
}
