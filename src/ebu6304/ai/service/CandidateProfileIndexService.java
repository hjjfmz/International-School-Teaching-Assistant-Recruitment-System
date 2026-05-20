package ebu6304.ai.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import ebu6304.ai.ResumeTextExtractor;
import ebu6304.ai.dto.CandidateProfileSourceDto;
import ebu6304.ai.repository.AiDatasetRepository;
import ebu6304.ai.util.AiTextUtils;
import ebu6304.ai.vo.CandidateProfileVo;
import ebu6304.model.Applicant;

public final class CandidateProfileIndexService {
    private final AiDatasetRepository repository;
    private final ResumeParseService resumeParseService;

    public CandidateProfileIndexService(AiDatasetRepository repository, ResumeParseService resumeParseService) {
        this.repository = repository;
        this.resumeParseService = resumeParseService;
    }

    public CandidateProfileVo ensureProfile(Applicant applicant) {
        CandidateProfileSourceDto source = toSource(applicant, true);
        String hash = sourceHash(applicant);
        AiDatasetRepository.CandidateCacheEntry cached = repository == null || applicant == null ? null : repository.getCandidateProfile(applicant.id());
        if (cached != null && hash.equals(cached.sourceHash()) && cached.profile() != null) {
            return cached.profile();
        }
        CandidateProfileVo profile = resumeParseService.parse(source);
        if (repository != null && applicant != null) repository.putCandidateProfile(applicant.id(), hash, profile);
        return profile;
    }

    public CandidateProfileVo getCachedOrLocalProfile(Applicant applicant) {
        String hash = sourceHash(applicant);
        AiDatasetRepository.CandidateCacheEntry cached = repository == null || applicant == null ? null : repository.getCandidateProfile(applicant.id());
        if (cached != null && hash.equals(cached.sourceHash()) && cached.profile() != null) {
            return cached.profile();
        }
        return resumeParseService.parseLocal(toSource(applicant, true));
    }

    public CandidateProfileVo warmProfile(Applicant applicant) {
        return ensureProfile(applicant);
    }

    public String sourceHash(Applicant applicant) {
        if (applicant == null) return "";
        String cvMeta = "";
        String cvPath = applicant.cvPath();
        if (cvPath != null && !cvPath.trim().isEmpty()) {
            try {
                Path path = Paths.get(cvPath.trim());
                if (Files.exists(path)) {
                    cvMeta = "|" + Files.size(path) + "|" + Files.getLastModifiedTime(path).toMillis();
                }
            } catch (Exception ignored) {
            }
        }
        return AiTextUtils.sourceHash(applicant.skills(), applicant.description(), applicant.cvPath(), cvMeta);
    }

    private CandidateProfileSourceDto toSource(Applicant applicant, boolean includeResumeText) {
        if (applicant == null) return new CandidateProfileSourceDto("", "", "", "");
        String resumeText = includeResumeText ? readResumeText(applicant.cvPath()) : "";
        return new CandidateProfileSourceDto(applicant.id(), applicant.skills(), applicant.description(), resumeText);
    }

    private static String readResumeText(String cvPath) {
        if (cvPath == null || cvPath.trim().isEmpty()) return "";
        try {
            return ResumeTextExtractor.extract(Paths.get(cvPath.trim()));
        } catch (Exception ex) {
            return "";
        }
    }
}
