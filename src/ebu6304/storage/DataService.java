package ebu6304.storage;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.AuthStore;

public final class DataService {
    private final Path dataDir;
    private final Path taInfoFile;
    private final Path moJobsFile;
    private final Path adminSystemFile;
    private final Path tempOperationFile;

    private final Map<String, Applicant> applicants = new HashMap<String, Applicant>();
    private final Map<String, Job> jobs = new HashMap<String, Job>();
    private final Map<String, Application> applications = new HashMap<String, Application>();

    public DataService() {
        this(Paths.get("data"));
    }

    public DataService(Path dataDir) {
        this.dataDir = dataDir;
        this.taInfoFile = dataDir.resolve("ta_info.csv");
        this.moJobsFile = dataDir.resolve("mo_jobs.json");
        this.adminSystemFile = dataDir.resolve("admin_system.xml");
        this.tempOperationFile = dataDir.resolve("temp_operation.txt");
    }

    public void init() {
        try {
            Files.createDirectories(dataDir);

            if (!Files.exists(taInfoFile)) {
                Files.write(taInfoFile,
                        ("id,name,email,skills,cvPath" + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                        StandardOpenOption.CREATE);
            }
            if (!Files.exists(moJobsFile)) {
                Files.write(moJobsFile, "{\"jobs\":[]}".getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE);
            }
            XmlStore.ensureAdminSystemXmlExists(adminSystemFile);
            if (!Files.exists(tempOperationFile)) Files.write(tempOperationFile, new byte[0], StandardOpenOption.CREATE);

            loadAll();
            if (jobs.isEmpty()) {
                seedDemoJobs();
            }
        } catch (IOException e) {
            OperationLog.append(tempOperationFile, "ERROR", "Init failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
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

    public synchronized boolean authenticate(String role, String account, String password) {
        return AuthStore.authenticate(adminSystemFile, role, account, password);
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

    public synchronized boolean deleteUser(String role, String account) {
        return AuthStore.deleteUser(adminSystemFile, role, account);
    }

    public synchronized boolean resetPassword(String role, String account, String newPassword) {
        Optional<AuthStore.User> u = AuthStore.findUser(adminSystemFile, role, account);
        if (!u.isPresent()) return false;
        upsertUser(role, account, newPassword, u.get().name());
        return true;
    }

    public synchronized Applicant upsertApplicantByAccount(String account, String name, String email, String skills, String cvPath) {
        Applicant a = new Applicant(account, name, email, skills, cvPath);
        applicants.put(a.id(), a);
        persistApplicants();
        return a;
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
        String id = UUID.randomUUID().toString();
        Job j = new Job(id, title, description, requiredSkills, hoursPerWeek, postedBy);
        jobs.put(id, j);
        persistJobs();
        return j;
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

        String id = UUID.randomUUID().toString();
        Application a = new Application(id, applicantId, jobId, Application.Status.SUBMITTED);
        applications.put(id, a);
        persistApplications();
        return a;
    }

    public synchronized void setApplicationStatus(String applicationId, Application.Status status) {
        Application a = applications.get(applicationId);
        if (a == null) return;
        applications.put(applicationId, a.withStatus(status));
        persistApplications();
    }

    public synchronized boolean withdrawApplication(String applicantId, String jobId) {
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

    private void seedDemoJobs() {
        createJob("TA - Software Engineering", "Support EBU6304 labs and tutorials", "Java,Git,Agile", 6, "MO");
        createJob("Invigilation Assistant", "Help with exam invigilation", "Attention to detail", 4, "Admin");
        createJob("TA - Databases (Support)", "Assist with Q&A and marking support", "SQL,Basics", 5, "MO");
    }

    private void loadAll() {
        applicants.clear();
        jobs.clear();
        applications.clear();
        loadApplicants();
        loadJobs();
    }

    private void loadApplicants() {
        List<String> lines = readAllLines(taInfoFile, StandardCharsets.UTF_8);
        for (int idx = 0; idx < lines.size(); idx++) {
            String line = lines.get(idx);
            if (line == null) continue;
            if (line.trim().isEmpty()) continue;
            if (idx == 0 && line.toLowerCase().startsWith("id,")) continue;
            String[] p = Csv.splitLine(line, 5);
            Applicant a = new Applicant(p[0], p[1], p[2], p[3], p[4]);
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

            if (id.isEmpty() || title.isEmpty()) continue;
            Job j = new Job(id, title, description, requiredSkills, hours, postedBy);
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
                String statusRaw = asString(am.get("status"));
                if (jobId.isEmpty()) jobId = id;
                Application.Status st;
                try {
                    st = Application.Status.valueOf(statusRaw);
                } catch (IllegalArgumentException iae) {
                    st = Application.Status.SUBMITTED;
                }
                if (appId.isEmpty() || applicantId.isEmpty() || jobId.isEmpty()) continue;
                applications.put(appId, new Application(appId, applicantId, jobId, st));
            }
        }
    }

    private void persistApplicants() {
        List<String> lines = new ArrayList<String>();
        lines.add("id,name,email,skills,cvPath");
        for (Applicant a : applicants.values()) {
            lines.add(Csv.join(a.id(), a.name(), a.email(), a.skills(), a.cvPath()));
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

    private void persistJobs() {
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

            List<Object> appsArr = new LinkedList<Object>();
            for (Application a : applications.values()) {
                if (!j.id().equals(a.jobId())) continue;
                Map<String, Object> am = new LinkedHashMap<String, Object>();
                am.put("id", a.id());
                am.put("applicantId", a.applicantId());
                am.put("jobId", a.jobId());
                am.put("status", a.status().name());
                appsArr.add(am);
            }
            jm.put("applications", appsArr);
            jobsArr.add(jm);
        }

        root.put("jobs", jobsArr);
        String json = MiniJson.stringify(root);
        try {
            Files.write(moJobsFile, json.getBytes(StandardCharsets.UTF_8), StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE);
        } catch (IOException e) {
            OperationLog.append(tempOperationFile, "ERROR", "Write mo_jobs.json failed: " + e.getMessage());
        }
    }

    private void persistApplications() {
        persistJobs();
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
}
