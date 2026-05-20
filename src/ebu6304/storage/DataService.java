package ebu6304.storage;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.AuthStore;

public final class DataService {
    public static final class Config {
        private final String dataPath;
        private final int passwordMinLength;
        private final String cvFormats;
        private final String defaultLang;

        public Config(String dataPath, int passwordMinLength, String cvFormats, String defaultLang) {
            this.dataPath = dataPath == null ? "" : dataPath;
            this.passwordMinLength = passwordMinLength <= 0 ? 6 : passwordMinLength;
            this.cvFormats = cvFormats == null ? "pdf,doc,docx" : cvFormats;
            this.defaultLang = defaultLang == null ? "EN" : defaultLang;
        }

        public String dataPath() { return dataPath; }
        public int passwordMinLength() { return passwordMinLength; }
        public String cvFormats() { return cvFormats; }
        public String defaultLang() { return defaultLang; }
    }

    private final Path dataDir;
    private final Path taInfoFile;
    private final Path moJobsFile;
    private final Path adminSystemFile;
    private final Path tempOperationFile;
    private final Path aiDatasetFile;

    private Config config = new Config("", 6, "pdf,doc,docx", "EN");

    private final Map<String, Applicant> applicants = new HashMap<String, Applicant>();
    private final Map<String, Job> jobs = new HashMap<String, Job>();
    private final Map<String, Application> applications = new HashMap<String, Application>();

    private static final Object APPLICANT_IO_LOCK = new Object();
    private static final Object JOB_IO_LOCK = new Object();
    private static final String JOB_ID_PREFIX = "JOB";
    private static final int JOB_ID_START = 10001;
    private static final String APPLICATION_ID_PREFIX = "APP";
    private static final int APPLICATION_ID_START = 10001;

    public DataService() {
        this(loadBootstrapDataDir());
    }

    public DataService(Path dataDir) {
        this.dataDir = dataDir;
        this.taInfoFile = dataDir.resolve("ta_info.csv");
        this.moJobsFile = dataDir.resolve("mo_jobs.json");
        this.adminSystemFile = dataDir.resolve("admin_system.xml");
        this.tempOperationFile = dataDir.resolve("temp_operation.txt");
        this.aiDatasetFile = dataDir.resolve("ai_dataset.json");
    }

    public void init() {
        try {
            Files.createDirectories(dataDir);

            if (!Files.exists(taInfoFile)) {
                Files.write(taInfoFile,
                        ("id,name,email,skills,cvPath,description" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE);
            }
            if (!Files.exists(moJobsFile)) {
                Files.write(moJobsFile, "{\"jobs\":[]}".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            }
            XmlStore.ensureAdminSystemXmlExists(adminSystemFile);
            AuthStore.migratePlaintextPasswords(adminSystemFile);
            if (!Files.exists(tempOperationFile)) Files.write(tempOperationFile, new byte[0], StandardOpenOption.CREATE);

            this.config = readConfig(adminSystemFile);

            loadAll();
        } catch (IOException e) {
            OperationLog.append(tempOperationFile, "ERROR", "Init failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public synchronized void reload() {
        try {
            loadAll();
        } catch (RuntimeException ex) {
            OperationLog.append(tempOperationFile, "ERROR", "Reload failed: " + ex.getMessage());
            throw ex;
        }
    }

    public synchronized Config getConfig() {
        return config;
    }

    public synchronized boolean updateConfig(String actor, Config newConfig) {
        if (newConfig == null) return false;
        boolean ok = writeConfig(adminSystemFile, newConfig);
        if (ok) this.config = newConfig;
        OperationLog.append(tempOperationFile, "INFO", "actor=" + (actor == null ? "" : actor) + " action=updateConfig ok=" + ok + " dataPath=" + newConfig.dataPath() + " passwordMinLength=" + newConfig.passwordMinLength() + " cvFormats=" + newConfig.cvFormats() + " defaultLang=" + newConfig.defaultLang());
        return ok;
    }

    public Path dataDir() {
        return dataDir;
    }

    public Path adminSystemFile() {
        return adminSystemFile;
    }

    public Path tempOperationFile() {
        return tempOperationFile;
    }

    private static Path loadBootstrapDataDir() {
        java.io.File projRoot = ebu6304.App.projectRoot();
        Path base = projRoot != null ? projRoot.toPath().resolve("data") : Paths.get("data");
        Path xml = base.resolve("admin_system.xml");
        if (!Files.exists(xml)) return base;
        try {
            Config cfg = readConfig(xml);
            String p = cfg.dataPath();
            if (p == null || p.trim().isEmpty()) return base;
            Path candidate = Paths.get(p.trim());
            return candidate;
        } catch (RuntimeException ex) {
            return base;
        }
    }

    private static Config readConfig(Path adminSystemXml) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc = b.parse(adminSystemXml.toFile());
            Element root = doc.getDocumentElement();
            if (root == null) return new Config("", 6, "pdf,doc,docx", "EN");
            Element cfg = firstChildElement(root, "config");
            if (cfg == null) return new Config("", 6, "pdf,doc,docx", "EN");
            String dataPath = cfg.getAttribute("dataPath");
            String pmlRaw = cfg.getAttribute("passwordMinLength");
            int pml = 6;
            try {
                if (pmlRaw != null && !pmlRaw.trim().isEmpty()) pml = Integer.parseInt(pmlRaw.trim());
            } catch (NumberFormatException ignored) {
            }
            String cvFormats = cfg.getAttribute("cvFormats");
            String defaultLang = cfg.getAttribute("defaultLang");
            return new Config(dataPath, pml, cvFormats, defaultLang);
        } catch (Exception e) {
            return new Config("", 6, "pdf,doc,docx", "EN");
        }
    }

    private static boolean writeConfig(Path adminSystemXml, Config c) {
        try {
            DocumentBuilderFactory f = DocumentBuilderFactory.newInstance();
            f.setNamespaceAware(false);
            DocumentBuilder b = f.newDocumentBuilder();
            Document doc = b.parse(adminSystemXml.toFile());
            Element root = doc.getDocumentElement();
            if (root == null) return false;
            Element cfg = firstChildElement(root, "config");
            if (cfg == null) {
                cfg = doc.createElement("config");
                root.appendChild(cfg);
            }
            cfg.setAttribute("dataPath", c.dataPath());
            cfg.setAttribute("passwordMinLength", String.valueOf(c.passwordMinLength()));
            cfg.setAttribute("cvFormats", c.cvFormats());
            cfg.setAttribute("defaultLang", c.defaultLang());
            XmlStore.write(adminSystemXml, doc);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private static Element firstChildElement(Element parent, String tagName) {
        if (parent == null) return null;
        NodeList kids = parent.getChildNodes();
        for (int i = 0; i < kids.getLength(); i++) {
            Node n = kids.item(i);
            if (!(n instanceof Element)) continue;
            Element e = (Element) n;
            if (tagName.equals(e.getTagName())) return e;
        }
        return null;
    }

    public synchronized boolean authenticate(String role, String account, String password) {
        return AuthStore.authenticate(adminSystemFile, role, account, password);
    }

    public synchronized Optional<String> authenticateAndGetRole(String account, String password) {
        return AuthStore.authenticateAndGetRole(adminSystemFile, account, password);
    }

    public synchronized void upsertUser(String role, String account, String password, String name) {
        AuthStore.upsertUser(adminSystemFile, new AuthStore.User(role, account, password, name));
    }

    public synchronized List<AuthStore.User> listUsers() {
        return AuthStore.listUsers(adminSystemFile);
    }

    public synchronized boolean setUserEnabled(String role, String account, boolean enabled) {
        return AuthStore.setEnabled(adminSystemFile, role, account, enabled);
    }

    public synchronized boolean setUserEnabled(String actor, String role, String account, boolean enabled) {
        boolean ok = AuthStore.setEnabled(adminSystemFile, role, account, enabled);
        OperationLog.append(tempOperationFile, "INFO", "actor=" + (actor == null ? "" : actor) + " action=setUserEnabled role=" + (role == null ? "" : role) + " account=" + (account == null ? "" : account) + " enabled=" + enabled + " ok=" + ok);
        return ok;
    }

    public synchronized boolean deleteUser(String account) {
        return AuthStore.deleteUser(adminSystemFile, account);
    }

    public synchronized boolean delete(String actor, String account) {
        String deletedRole = "";
        for (AuthStore.User user : AuthStore.listUsers(adminSystemFile)) {
            if (account != null && account.equals(user.account())) {
                deletedRole = user.role();
                break;
            }
        }
        boolean deleted = AuthStore.deleteUser(adminSystemFile, account);
        boolean deletedApplicant = deleteApplicantByAccount(account);
        int removedApps = removeApplicationsForApplicant(account);
        if (removedApps > 0) persistApplications();
        boolean ok = deleted || deletedApplicant || removedApps > 0;
        OperationLog.append(tempOperationFile, "INFO",
                "actor=" + safeLogValue(actor) +
                " action=deleteUser account=" + safeLogValue(account) +
                " role=" + safeLogValue(deletedRole) +
                " deletedUser=" + deleted +
                " deletedApplicant=" + deletedApplicant +
                " removedApplications=" + removedApps);
        return ok;
    }

    public synchronized boolean resetPassword(String role, String account, String newPassword) {
        Optional<AuthStore.User> u = AuthStore.findUser(adminSystemFile, role, account);
        if (!u.isPresent()) return false;
        upsertUser(role, account, newPassword, u.get().name());
        OperationLog.append(tempOperationFile, "INFO",
                "actor=" + safeLogValue(account) +
                " action=resetPassword role=" + safeLogValue(role) +
                " account=" + safeLogValue(account));
        return true;
    }

    public synchronized Applicant upsertApplicantByAccount(String account, String name, String email, String skills, String cvPath) {
        Applicant existing = applicants.get(account);
        String desc = existing != null ? existing.description() : "";
        Applicant a = new Applicant(account, name, email, skills, cvPath, desc);
        applicants.put(a.id(), a);
        persistApplicants();
        resetApplicantAiScores(a.id());
        return a;
    }

    public synchronized String storeCv(String applicantId, String sourcePath) {
        if (applicantId == null || applicantId.trim().isEmpty()) throw new IllegalArgumentException("applicantId");
        if (sourcePath == null || sourcePath.trim().isEmpty()) throw new IllegalArgumentException("sourcePath");

        try {
            Path src = Paths.get(sourcePath);
            if (!Files.exists(src)) throw new IOException("CV file not found");

            String safeId = toSafeFileToken(applicantId);
            String ext = getFileExt(sourcePath);
            Path cvDir = dataDir.resolve("cv");
            Files.createDirectories(cvDir);
            Path dest = cvDir.resolve(safeId + (ext.isEmpty() ? "" : ("." + ext)));
            Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
            resetApplicantAiScores(applicantId);
            return dest.toAbsolutePath().toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void resetApplicantAiScores(String applicantId) {
        if (applicantId == null || applicantId.trim().isEmpty()) return;
        boolean changed = false;
        for (Map.Entry<String, Application> entry : applications.entrySet()) {
            Application app = entry.getValue();
            if (!applicantId.equals(app.applicantId())) continue;
            if (app.aiScore() < 0) continue;
            entry.setValue(app.withAiScore(-1));
            changed = true;
        }
        if (changed) {
            persistApplications();
            OperationLog.append(tempOperationFile, "INFO",
                    "actor=" + safeLogValue(applicantId) +
                    " action=resetApplicantAiScores applicantId=" + safeLogValue(applicantId));
        }
    }

    public synchronized boolean deleteApplicantByAccount(String account) {
        if (account == null) return false;
        Applicant removed = applicants.remove(account);
        if (removed == null) return false;
        persistApplicants();
        return true;
    }

    public synchronized List<Applicant> listApplicants() {
        List<Applicant> out = new ArrayList<Applicant>(applicants.values());
        Collections.sort(out, new Comparator<Applicant>() {
            @Override
            public int compare(Applicant o1, Applicant o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.name(), o2.name());
            }
        });
        return out;
    }

    public synchronized Optional<Applicant> getApplicant(String id) {
        return Optional.ofNullable(applicants.get(id));
    }

    public synchronized Applicant createApplicant(String name, String email) {
        String id = UUID.randomUUID().toString();
        Applicant a = new Applicant(id, name, email, "", "");
        applicants.put(id, a);
        persistApplicants();
        return a;
    }

    public synchronized void upsertApplicant(Applicant applicant) {
        applicants.put(applicant.id(), applicant);
        persistApplicants();
        resetApplicantAiScores(applicant.id());
    }

    private static String getFileExt(String path) {
        if (path == null) return "";
        String p = path.trim();
        int dot = p.lastIndexOf('.');
        if (dot < 0) return "";
        String ext = p.substring(dot + 1).trim().toLowerCase();
        if (ext.length() > 10) return "";
        return ext;
    }

    private static String toSafeFileToken(String raw) {
        String s = raw == null ? "" : raw.trim();
        if (s.isEmpty()) return "cv";
        return s.replaceAll("[^a-zA-Z0-9._-]", "_");
    }

    public synchronized List<Job> listJobs() {
        List<Job> out = new ArrayList<Job>(jobs.values());
        Collections.sort(out, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.title(), o2.title());
            }
        });
        return out;
    }

    public synchronized Optional<Job> getJob(String id) {
        return Optional.ofNullable(jobs.get(id));
    }

    public synchronized Job createJob(String title, String description, String requiredSkills, int hoursPerWeek, String postedBy) {
        return createJob(postedBy, title, description, requiredSkills, hoursPerWeek, postedBy);
    }

    public synchronized Job createJob(String actor, String title, String description, String requiredSkills, int hoursPerWeek, String postedBy) {
        String id = nextAvailableJobId();
        Job j = new Job(id, title, description, requiredSkills, hoursPerWeek, postedBy);
        jobs.put(id, j);
        persistJobs();
        OperationLog.append(tempOperationFile, "INFO",
                "actor=" + safeLogValue(actor) +
                " action=createJob jobId=" + id +
                " postedBy=" + safeLogValue(postedBy) +
                " title=" + safeLogValue(title) +
                " hoursPerWeek=" + hoursPerWeek);
        return j;
    }

    public synchronized boolean setJobStatus(String actor, String jobId, Job.Status status) {
        if (jobId == null || status == null) return false;
        Job j = jobs.get(jobId);
        if (j == null) return false;
        jobs.put(jobId, j.withStatus(status));
        persistJobs();
        OperationLog.append(tempOperationFile, "INFO", "actor=" + (actor == null ? "" : actor) + " action=setJobStatus jobId=" + jobId + " status=" + status.name());
        return true;
    }

    public synchronized boolean setJobCategory(String actor, String jobId, String category) {
        if (jobId == null) return false;
        Job j = jobs.get(jobId);
        if (j == null) return false;
        jobs.put(jobId, j.withCategory(category == null ? "" : category));
        persistJobs();
        OperationLog.append(tempOperationFile, "INFO", "actor=" + (actor == null ? "" : actor) + " action=setJobCategory jobId=" + jobId + " category=" + (category == null ? "" : category));
        return true;
    }

    public synchronized List<Application> listApplicationsForApplicant(String applicantId) {
        List<Application> out = new ArrayList<Application>();
        for (Application a : applications.values()) {
            if (a.applicantId().equals(applicantId)) out.add(a);
        }
        Collections.sort(out, new Comparator<Application>() {
            @Override
            public int compare(Application o1, Application o2) {
                return o1.id().compareTo(o2.id());
            }
        });
        return out;
    }

    public synchronized List<Application> listApplicationsForJob(String jobId) {
        List<Application> out = new ArrayList<Application>();
        for (Application a : applications.values()) {
            if (a.jobId().equals(jobId)) out.add(a);
        }
        Collections.sort(out, new Comparator<Application>() {
            @Override
            public int compare(Application o1, Application o2) {
                return o1.id().compareTo(o2.id());
            }
        });
        return out;
    }

    public synchronized Optional<Application> findApplication(String applicantId, String jobId) {
        for (Application a : applications.values()) {
            if (a.applicantId().equals(applicantId) && a.jobId().equals(jobId)) return Optional.of(a);
        }
        return Optional.empty();
    }

    public synchronized Application submitApplication(String applicantId, String jobId) {
        Optional<Application> existing = findApplication(applicantId, jobId);
        if (existing.isPresent()) return existing.get();

        String id = nextAvailableApplicationId();
        Application a = new Application(id, applicantId, jobId, Application.Status.SUBMITTED, System.currentTimeMillis(), -1);
        applications.put(id, a);
        persistApplications();
        OperationLog.append(tempOperationFile, "INFO",
                "actor=" + safeLogValue(applicantId) +
                " action=submitApplication applicationId=" + id +
                " applicantId=" + safeLogValue(applicantId) +
                " jobId=" + safeLogValue(jobId));
        return a;
    }

    public synchronized void setApplicationStatus(String applicationId, Application.Status status) {
        setApplicationStatus("", applicationId, status);
    }

    public synchronized void setApplicationStatus(String actor, String applicationId, Application.Status status) {
        Application a = applications.get(applicationId);
        if (a == null || status == null) return;
        applications.put(applicationId, a.withStatus(status));
        persistApplications();
        OperationLog.append(tempOperationFile, "INFO",
                "actor=" + safeLogValue(actor) +
                " action=setApplicationStatus applicationId=" + safeLogValue(applicationId) +
                " applicantId=" + safeLogValue(a.applicantId()) +
                " jobId=" + safeLogValue(a.jobId()) +
                " fromStatus=" + a.status().name() +
                " toStatus=" + status.name());
    }

    public synchronized void updateApplicationAiScore(String applicationId, int score) {
        Application a = applications.get(applicationId);
        if (a == null) return;
        if (a.aiScore() == score) return;
        applications.put(applicationId, a.withAiScore(score));
        persistApplications();
    }

    public synchronized boolean withdrawApplication(String applicantId, String jobId) {
        return withdrawApplication(applicantId, jobId, applicantId);
    }

    public synchronized boolean withdrawApplication(String applicantId, String jobId, String actor) {
        if (applicantId == null || jobId == null) return false;
        Application target = null;
        for (Application a : applications.values()) {
            if (applicantId.equals(a.applicantId()) && jobId.equals(a.jobId())) {
                target = a;
                break;
            }
        }
        if (target == null) return false;
        if (target.status() != Application.Status.SUBMITTED) return false;
        applications.remove(target.id());
        persistApplications();
        OperationLog.append(tempOperationFile, "INFO",
                "actor=" + safeLogValue(actor) +
                " action=withdrawApplication applicationId=" + safeLogValue(target.id()) +
                " applicantId=" + safeLogValue(applicantId) +
                " jobId=" + safeLogValue(jobId));
        return true;
    }

    public synchronized int acceptedWeeklyHoursForApplicant(String applicantId) {
        int sum = 0;
        for (Application a : applications.values()) {
            if (!a.applicantId().equals(applicantId)) continue;
            if (a.status() != Application.Status.ACCEPTED) continue;
            Job j = jobs.get(a.jobId());
            if (j != null) sum += j.hoursPerWeek();
        }
        return sum;
    }

    private void loadAll() {
        applicants.clear();
        jobs.clear();
        applications.clear();
        loadApplicants();
        loadJobs();
        normalizeJobIdsIfNeeded();
        normalizeApplicationIdsIfNeeded();
    }

    private void loadApplicants() {
        List<String> lines = readAllLines(taInfoFile, StandardCharsets.UTF_8);
        for (int idx = 0; idx < lines.size(); idx++) {
            String line = lines.get(idx);
            if (line == null) continue;
            if (line.trim().isEmpty()) continue;
            if (idx == 0 && line.toLowerCase().startsWith("id,")) continue;
            String[] p = Csv.splitLine(line, 6);
            Applicant a = new Applicant(p[0], p[1], p[2], p[3], p[4], p[5]);
            applicants.put(a.id(), a);
        }
    }

    private void loadJobs() {
        String json;
        try {
            json = new String(Files.readAllBytes(moJobsFile), StandardCharsets.UTF_8);
        } catch (IOException e) {
            OperationLog.append(tempOperationFile, "ERROR", "Read mo_jobs.json failed: " + e.getMessage());
            return;
        }

        Object rootObj;
        try {
            rootObj = MiniJson.parse(json);
        } catch (RuntimeException ex) {
            OperationLog.append(tempOperationFile, "ERROR", "Parse mo_jobs.json failed: " + ex.getMessage());
            return;
        }

        if (!(rootObj instanceof Map)) return;
        @SuppressWarnings("unchecked")
        Map<String, Object> root = (Map<String, Object>) rootObj;
        Object jobsArrObj = root.get("jobs");
        if (!(jobsArrObj instanceof List)) return;

        @SuppressWarnings("unchecked")
        List<Object> jobsArr = (List<Object>) jobsArrObj;
        for (Object jo : jobsArr) {
            if (!(jo instanceof Map)) continue;
            @SuppressWarnings("unchecked")
            Map<String, Object> jm = (Map<String, Object>) jo;
            String id = asString(jm.get("id"));
            String title = asString(jm.get("title"));
            String description = asString(jm.get("description"));
            String requiredSkills = asString(jm.get("requiredSkills"));
            int hours = asInt(jm.get("hoursPerWeek"));
            String postedBy = asString(jm.get("postedBy"));
            String statusRaw = asString(jm.get("status"));
            String category = asString(jm.get("category"));

            if (id.isEmpty() || title.isEmpty()) continue;
            Job.Status st;
            try {
                st = Job.Status.valueOf(statusRaw);
            } catch (IllegalArgumentException iae) {
                st = Job.Status.OPEN;
            }
            Job j = new Job(id, title, description, requiredSkills, hours, postedBy, st, category);
            jobs.put(j.id(), j);

            Object appsObj = jm.get("applications");
            if (!(appsObj instanceof List)) continue;
            @SuppressWarnings("unchecked")
            List<Object> appsArr = (List<Object>) appsObj;
            for (Object ao : appsArr) {
                if (!(ao instanceof Map)) continue;
                @SuppressWarnings("unchecked")
                Map<String, Object> am = (Map<String, Object>) ao;
                String appId = asString(am.get("id"));
                String applicantId = asString(am.get("applicantId"));
                String jobId = asString(am.get("jobId"));
                String appStatusRaw = asString(am.get("status"));
                long createdAt = asLong(am.get("createdAt"));
                int aiScore = asInt(am.get("aiScore")); // New field
                if (jobId.isEmpty()) jobId = id;
                Application.Status appSt;
                try {
                    appSt = Application.Status.valueOf(appStatusRaw);
                } catch (IllegalArgumentException iae) {
                    appSt = Application.Status.SUBMITTED;
                }
                if (appId.isEmpty() || applicantId.isEmpty() || jobId.isEmpty()) continue;
                if (createdAt <= 0) createdAt = System.currentTimeMillis();
                if (aiScore == 0 && am.get("aiScore") == null) aiScore = -1; // Default for old data
                applications.put(appId, new Application(appId, applicantId, jobId, appSt, createdAt, aiScore));
            }
        }
    }

    private void persistApplicants() {
        synchronized (APPLICANT_IO_LOCK) {
            List<String> lines = new ArrayList<String>();
            lines.add("id,name,email,skills,cvPath,description");
            for (Applicant a : applicants.values()) {
                lines.add(Csv.join(a.id(), a.name(), a.email(), a.skills(), a.cvPath(), a.description()));
            }
            if (lines.size() > 1) {
                List<String> dataLines = new ArrayList<String>(lines.subList(1, lines.size()));
                Collections.sort(dataLines);
                List<String> out = new ArrayList<String>();
                out.add(lines.get(0));
                out.addAll(dataLines);
                writeAllLines(taInfoFile, out, StandardCharsets.UTF_8);
            } else {
                writeAllLines(taInfoFile, lines, StandardCharsets.UTF_8);
            }
        }
    }

    private void persistJobs() {
        synchronized (JOB_IO_LOCK) {
            Map<String, Object> root = new LinkedHashMap<String, Object>();
            List<Object> jobsArr = new LinkedList<Object>();

            List<Job> jobList = new ArrayList<Job>(jobs.values());
            Collections.sort(jobList, new Comparator<Job>() {
                @Override
                public int compare(Job o1, Job o2) {
                    return String.CASE_INSENSITIVE_ORDER.compare(o1.title(), o2.title());
                }
            });

            for (Job j : jobList) {
                Map<String, Object> jm = new LinkedHashMap<String, Object>();
                jm.put("id", j.id());
                jm.put("title", j.title());
                jm.put("description", j.description());
                jm.put("requiredSkills", j.requiredSkills());
                jm.put("hoursPerWeek", Integer.valueOf(j.hoursPerWeek()));
                jm.put("postedBy", j.postedBy());
                jm.put("status", j.status().name());
                jm.put("category", j.category());

                List<Object> appsArr = new LinkedList<Object>();
                for (Application a : applications.values()) {
                    if (!j.id().equals(a.jobId())) continue;
                    Map<String, Object> am = new LinkedHashMap<String, Object>();
                    am.put("id", a.id());
                    am.put("applicantId", a.applicantId());
                    am.put("jobId", a.jobId());
                    am.put("status", a.status().name());
                    am.put("createdAt", Long.valueOf(a.createdAt()));
                    am.put("aiScore", Integer.valueOf(a.aiScore()));
                    appsArr.add(am);
                }
                jm.put("applications", appsArr);
                jobsArr.add(jm);
            }

            root.put("jobs", jobsArr);
            String json = MiniJson.stringifyPretty(root) + System.lineSeparator();
            try {
                Files.write(moJobsFile, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING,
                        StandardOpenOption.CREATE);
            } catch (IOException e) {
                OperationLog.append(tempOperationFile, "ERROR", "Write mo_jobs.json failed: " + e.getMessage());
            }
        }
    }

    private void persistApplications() {
        persistJobs();
    }

    private void normalizeJobIdsIfNeeded() {
        JobIdMigration migration = planJobIdMigration();
        if (!migration.changed()) return;

        jobs.clear();
        jobs.putAll(migration.jobs());
        remapApplications(migration.aliases());
        persistJobs();
        rewriteAiDatasetJobIds(migration.aliases());
        rewriteOperationLogIds(migration.aliases(), new LinkedHashMap<String, String>());
        OperationLog.append(tempOperationFile, "INFO",
                "actor=system action=normalizeJobIds migrated=" + migration.aliases().size());
    }

    private JobIdMigration planJobIdMigration() {
        List<Job> jobList = new ArrayList<Job>(jobs.values());
        Collections.sort(jobList, new Comparator<Job>() {
            @Override
            public int compare(Job a, Job b) {
                int byTitle = String.CASE_INSENSITIVE_ORDER.compare(a.title(), b.title());
                if (byTitle != 0) return byTitle;
                return String.CASE_INSENSITIVE_ORDER.compare(a.id(), b.id());
            }
        });

        Map<String, List<Job>> grouped = new LinkedHashMap<String, List<Job>>();
        for (Job job : jobList) {
            String signature = jobSignature(job);
            List<Job> sameJobs = grouped.get(signature);
            if (sameJobs == null) {
                sameJobs = new ArrayList<Job>();
                grouped.put(signature, sameJobs);
            }
            sameJobs.add(job);
        }

        Set<String> reservedIds = new HashSet<String>();
        for (List<Job> group : grouped.values()) {
            String modernId = chooseModernJobId(group);
            if (!modernId.isEmpty()) reservedIds.add(modernId);
        }

        int nextSeq = nextJobSequence(reservedIds);
        Map<String, Job> normalizedJobs = new LinkedHashMap<String, Job>();
        Map<String, String> aliases = new LinkedHashMap<String, String>();
        for (List<Job> group : grouped.values()) {
            String canonicalId = chooseModernJobId(group);
            if (canonicalId.isEmpty()) canonicalId = formatJobId(nextSeq++);

            Job preferred = choosePreferredJob(group);
            normalizedJobs.put(canonicalId, copyJobWithId(preferred, canonicalId));
            for (Job job : group) {
                if (!canonicalId.equals(job.id())) aliases.put(job.id(), canonicalId);
            }
        }
        return new JobIdMigration(normalizedJobs, aliases);
    }

    private void remapApplications(Map<String, String> aliases) {
        if (aliases == null || aliases.isEmpty()) return;
        Map<String, Application> remapped = new LinkedHashMap<String, Application>();
        for (Application app : applications.values()) {
            String newJobId = aliases.get(app.jobId());
            if (newJobId != null && !newJobId.equals(app.jobId())) {
                app = new Application(app.id(), app.applicantId(), newJobId, app.status(), app.createdAt(), app.aiScore());
            }
            remapped.put(app.id(), app);
        }
        applications.clear();
        applications.putAll(remapped);
    }

    private void normalizeApplicationIdsIfNeeded() {
        ApplicationIdMigration migration = planApplicationIdMigration();
        if (!migration.changed()) return;

        applications.clear();
        applications.putAll(migration.applications());
        persistApplications();
        rewriteOperationLogIds(new LinkedHashMap<String, String>(), migration.aliases());
        OperationLog.append(tempOperationFile, "INFO",
                "actor=system action=normalizeApplicationIds migrated=" + migration.aliases().size());
    }

    private ApplicationIdMigration planApplicationIdMigration() {
        List<Application> applicationList = new ArrayList<Application>(applications.values());
        Collections.sort(applicationList, new Comparator<Application>() {
            @Override
            public int compare(Application a, Application b) {
                int byTime = Long.compare(a.createdAt(), b.createdAt());
                if (byTime != 0) return byTime;
                int byApplicant = String.CASE_INSENSITIVE_ORDER.compare(a.applicantId(), b.applicantId());
                if (byApplicant != 0) return byApplicant;
                int byJob = String.CASE_INSENSITIVE_ORDER.compare(a.jobId(), b.jobId());
                if (byJob != 0) return byJob;
                return String.CASE_INSENSITIVE_ORDER.compare(a.id(), b.id());
            }
        });

        Map<String, List<Application>> grouped = new LinkedHashMap<String, List<Application>>();
        for (Application app : applicationList) {
            String signature = applicationSignature(app);
            List<Application> sameApps = grouped.get(signature);
            if (sameApps == null) {
                sameApps = new ArrayList<Application>();
                grouped.put(signature, sameApps);
            }
            sameApps.add(app);
        }

        Set<String> reservedIds = new HashSet<String>();
        for (List<Application> group : grouped.values()) {
            String modernId = chooseModernApplicationId(group);
            if (!modernId.isEmpty()) reservedIds.add(modernId);
        }

        int nextSeq = nextApplicationSequence(reservedIds);
        Map<String, Application> normalizedApps = new LinkedHashMap<String, Application>();
        Map<String, String> aliases = new LinkedHashMap<String, String>();
        for (List<Application> group : grouped.values()) {
            String canonicalId = chooseModernApplicationId(group);
            if (canonicalId.isEmpty()) canonicalId = formatApplicationId(nextSeq++);

            Application preferred = choosePreferredApplication(group);
            normalizedApps.put(canonicalId, copyApplicationWithId(preferred, canonicalId));
            for (Application app : group) {
                if (!canonicalId.equals(app.id())) aliases.put(app.id(), canonicalId);
            }
        }
        return new ApplicationIdMigration(normalizedApps, aliases);
    }

    private void rewriteAiDatasetJobIds(Map<String, String> aliases) {
        if (aliases == null || aliases.isEmpty() || !Files.exists(aiDatasetFile)) return;
        try {
            Object rootObj = MiniJson.parse(new String(Files.readAllBytes(aiDatasetFile), StandardCharsets.UTF_8));
            if (!(rootObj instanceof Map)) return;
            @SuppressWarnings("unchecked")
            Map<String, Object> root = (Map<String, Object>) rootObj;
            boolean changed = false;
            changed |= remapAiDatasetJobProfiles(root, aliases);
            changed |= remapAiDatasetNotes(root, aliases);
            if (!changed) return;
            Files.write(aiDatasetFile,
                    (MiniJson.stringifyPretty(root) + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (Exception e) {
            OperationLog.append(tempOperationFile, "WARN", "Rewrite ai_dataset.json failed: " + e.getMessage());
        }
    }

    private void rewriteOperationLogIds(Map<String, String> jobAliases, Map<String, String> appAliases) {
        boolean hasJobAliases = jobAliases != null && !jobAliases.isEmpty();
        boolean hasAppAliases = appAliases != null && !appAliases.isEmpty();
        if ((!hasJobAliases && !hasAppAliases) || !Files.exists(tempOperationFile)) return;
        try {
            String raw = new String(Files.readAllBytes(tempOperationFile), StandardCharsets.UTF_8);
            String updated = raw;
            if (hasJobAliases) {
                for (Map.Entry<String, String> entry : jobAliases.entrySet()) {
                    updated = updated.replace(entry.getKey(), entry.getValue());
                }
            }
            if (hasAppAliases) {
                for (Map.Entry<String, String> entry : appAliases.entrySet()) {
                    updated = updated.replace(entry.getKey(), entry.getValue());
                }
            }
            if (safeEquals(raw, updated)) return;
            Files.write(tempOperationFile, updated.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            OperationLog.append(tempOperationFile, "WARN", "Rewrite temp_operation.txt failed: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private boolean remapAiDatasetJobProfiles(Map<String, Object> root, Map<String, String> aliases) {
        Object value = root.get("jobProfiles");
        if (!(value instanceof List)) return false;
        List<Object> items = (List<Object>) value;
        Map<String, Map<String, Object>> dedup = new LinkedHashMap<String, Map<String, Object>>();
        boolean changed = false;
        for (Object item : items) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            String oldJobId = asString(map.get("jobId"));
            String newJobId = aliases.containsKey(oldJobId) ? aliases.get(oldJobId) : oldJobId;
            if (!safeEquals(oldJobId, newJobId)) {
                map.put("jobId", newJobId);
                changed = true;
            }
            Object profileObj = map.get("profile");
            if (profileObj instanceof Map) {
                Map<String, Object> profile = (Map<String, Object>) profileObj;
                if (!safeEquals(asString(profile.get("jobId")), newJobId)) {
                    profile.put("jobId", newJobId);
                    changed = true;
                }
            }
            Map<String, Object> existing = dedup.get(newJobId);
            if (existing == null || asLong(map.get("updatedAt")) >= asLong(existing.get("updatedAt"))) {
                dedup.put(newJobId, map);
            }
        }
        if (items.size() != dedup.size()) changed = true;
        root.put("jobProfiles", new ArrayList<Object>(dedup.values()));
        return changed;
    }

    @SuppressWarnings("unchecked")
    private boolean remapAiDatasetNotes(Map<String, Object> root, Map<String, String> aliases) {
        Object value = root.get("recommendationNotes");
        if (!(value instanceof List)) return false;
        List<Object> items = (List<Object>) value;
        Map<String, Map<String, Object>> dedup = new LinkedHashMap<String, Map<String, Object>>();
        boolean changed = false;
        for (Object item : items) {
            if (!(item instanceof Map)) continue;
            Map<String, Object> map = (Map<String, Object>) item;
            String candidateId = asString(map.get("candidateId"));
            String oldJobId = asString(map.get("jobId"));
            String newJobId = aliases.containsKey(oldJobId) ? aliases.get(oldJobId) : oldJobId;
            if (!safeEquals(oldJobId, newJobId)) {
                map.put("jobId", newJobId);
                changed = true;
            }
            dedup.put(candidateId + "::" + newJobId, map);
        }
        if (items.size() != dedup.size()) changed = true;
        root.put("recommendationNotes", new ArrayList<Object>(dedup.values()));
        return changed;
    }

    private String nextAvailableJobId() {
        Set<String> usedIds = new HashSet<String>(jobs.keySet());
        return formatJobId(nextJobSequence(usedIds));
    }

    private String nextAvailableApplicationId() {
        Set<String> usedIds = new HashSet<String>(applications.keySet());
        return formatApplicationId(nextApplicationSequence(usedIds));
    }

    private static int nextJobSequence(Iterable<String> ids) {
        int next = JOB_ID_START;
        if (ids == null) return next;
        for (String id : ids) {
            int seq = parseJobIdSequence(id);
            if (seq >= next) next = seq + 1;
        }
        return next;
    }

    private static int nextApplicationSequence(Iterable<String> ids) {
        int next = APPLICATION_ID_START;
        if (ids == null) return next;
        for (String id : ids) {
            int seq = parseApplicationIdSequence(id);
            if (seq >= next) next = seq + 1;
        }
        return next;
    }

    private static String chooseModernJobId(List<Job> jobs) {
        String best = "";
        if (jobs == null) return best;
        for (Job job : jobs) {
            if (!isFriendlyJobId(job.id())) continue;
            if (best.isEmpty() || parseJobIdSequence(job.id()) < parseJobIdSequence(best)) {
                best = job.id();
            }
        }
        return best;
    }

    private static Job choosePreferredJob(List<Job> jobs) {
        Job best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Job job : jobs) {
            int score = 0;
            if (isFriendlyJobId(job.id())) score += 1000;
            if (job.status() != Job.Status.OPEN) score += 100;
            if (!job.category().trim().isEmpty()) score += 10;
            if (!job.description().trim().isEmpty()) score += 1;
            if (best == null || score > bestScore
                    || (score == bestScore && String.CASE_INSENSITIVE_ORDER.compare(job.id(), best.id()) < 0)) {
                best = job;
                bestScore = score;
            }
        }
        return best == null ? jobs.get(0) : best;
    }

    private static String chooseModernApplicationId(List<Application> applications) {
        String best = "";
        if (applications == null) return best;
        for (Application app : applications) {
            if (!isFriendlyApplicationId(app.id())) continue;
            if (best.isEmpty() || parseApplicationIdSequence(app.id()) < parseApplicationIdSequence(best)) {
                best = app.id();
            }
        }
        return best;
    }

    private static Application choosePreferredApplication(List<Application> applications) {
        Application best = null;
        int bestScore = Integer.MIN_VALUE;
        for (Application app : applications) {
            int score = 0;
            if (isFriendlyApplicationId(app.id())) score += 1000;
            if (app.createdAt() > 0L) score += 100;
            if (app.aiScore() >= 0) score += 10;
            if (app.status() != Application.Status.SUBMITTED) score += 1;
            if (best == null || score > bestScore
                    || (score == bestScore && String.CASE_INSENSITIVE_ORDER.compare(app.id(), best.id()) < 0)) {
                best = app;
                bestScore = score;
            }
        }
        return best == null ? applications.get(0) : best;
    }

    private static Job copyJobWithId(Job job, String newId) {
        return new Job(newId, job.title(), job.description(), job.requiredSkills(), job.hoursPerWeek(),
                job.postedBy(), job.status(), job.category());
    }

    private static Application copyApplicationWithId(Application app, String newId) {
        return new Application(newId, app.applicantId(), app.jobId(), app.status(), app.createdAt(), app.aiScore());
    }

    private static String formatJobId(int sequence) {
        return JOB_ID_PREFIX + Math.max(sequence, JOB_ID_START);
    }

    private static String formatApplicationId(int sequence) {
        return APPLICATION_ID_PREFIX + Math.max(sequence, APPLICATION_ID_START);
    }

    private static boolean isFriendlyJobId(String id) {
        return parseJobIdSequence(id) > 0;
    }

    private static boolean isFriendlyApplicationId(String id) {
        return parseApplicationIdSequence(id) > 0;
    }

    private static int parseJobIdSequence(String id) {
        if (id == null) return -1;
        String value = id.trim();
        if (!value.startsWith(JOB_ID_PREFIX) || value.length() <= JOB_ID_PREFIX.length()) return -1;
        String digits = value.substring(JOB_ID_PREFIX.length());
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) return -1;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static int parseApplicationIdSequence(String id) {
        if (id == null) return -1;
        String value = id.trim();
        if (!value.startsWith(APPLICATION_ID_PREFIX) || value.length() <= APPLICATION_ID_PREFIX.length()) return -1;
        String digits = value.substring(APPLICATION_ID_PREFIX.length());
        for (int i = 0; i < digits.length(); i++) {
            if (!Character.isDigit(digits.charAt(i))) return -1;
        }
        try {
            return Integer.parseInt(digits);
        } catch (NumberFormatException ex) {
            return -1;
        }
    }

    private static String jobSignature(Job job) {
        return normalizeKey(job.title()) + "\u001F"
                + normalizeKey(job.description()) + "\u001F"
                + normalizeKey(job.requiredSkills()) + "\u001F"
                + job.hoursPerWeek() + "\u001F"
                + normalizeKey(job.postedBy());
    }

    private static String applicationSignature(Application app) {
        return normalizeKey(app.applicantId()) + "\u001F"
                + normalizeKey(app.jobId()) + "\u001F"
                + app.createdAt() + "\u001F"
                + app.status().name();
    }

    private static String normalizeKey(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ").toLowerCase();
    }

    private static final class JobIdMigration {
        private final Map<String, Job> jobs;
        private final Map<String, String> aliases;

        private JobIdMigration(Map<String, Job> jobs, Map<String, String> aliases) {
            this.jobs = jobs == null ? new LinkedHashMap<String, Job>() : new LinkedHashMap<String, Job>(jobs);
            this.aliases = aliases == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(aliases);
        }

        private Map<String, Job> jobs() {
            return new LinkedHashMap<String, Job>(jobs);
        }

        private Map<String, String> aliases() {
            return new LinkedHashMap<String, String>(aliases);
        }

        private boolean changed() {
            return !aliases.isEmpty();
        }
    }

    private static final class ApplicationIdMigration {
        private final Map<String, Application> applications;
        private final Map<String, String> aliases;

        private ApplicationIdMigration(Map<String, Application> applications, Map<String, String> aliases) {
            this.applications = applications == null ? new LinkedHashMap<String, Application>() : new LinkedHashMap<String, Application>(applications);
            this.aliases = aliases == null ? new LinkedHashMap<String, String>() : new LinkedHashMap<String, String>(aliases);
        }

        private Map<String, Application> applications() {
            return new LinkedHashMap<String, Application>(applications);
        }

        private Map<String, String> aliases() {
            return new LinkedHashMap<String, String>(aliases);
        }

        private boolean changed() {
            return !aliases.isEmpty();
        }
    }

    private int removeApplicationsForApplicant(String applicantId) {
        if (applicantId == null) return 0;
        int removed = 0;
        List<String> ids = new ArrayList<String>();
        for (Application a : applications.values()) {
            if (applicantId.equals(a.applicantId())) ids.add(a.id());
        }
        for (String id : ids) {
            if (applications.remove(id) != null) removed++;
        }
        return removed;
    }

    private static List<String> readAllLines(Path p, Charset cs) {
        try {
            return Files.readAllLines(p, cs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeAllLines(Path p, List<String> lines, Charset cs) {
        try {
            Files.write(p, lines, cs, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String asString(Object v) {
        if (v == null) return "";
        if (v instanceof String) return (String) v;
        return String.valueOf(v);
    }

    private static int asInt(Object v) {
        if (v == null) return 0;
        if (v instanceof Number) return ((Number) v).intValue();
        try {
            return Integer.parseInt(String.valueOf(v));
        } catch (NumberFormatException nfe) {
            return 0;
        }
    }

    private static long asLong(Object v) {
        if (v == null) return 0L;
        if (v instanceof Number) return ((Number) v).longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException nfe) {
            return 0L;
        }
    }

    private static boolean safeEquals(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }

    private static String safeLogValue(String value) {
        if (value == null) return "";
        return value.trim().replaceAll("\\s+", "_");
    }
}
