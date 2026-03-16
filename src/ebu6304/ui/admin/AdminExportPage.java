package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.Csv;
import ebu6304.storage.DataService;
import ebu6304.storage.MiniJson;
import ebu6304.storage.OperationLog;

public final class AdminExportPage extends JPanel {
    private final DataService data;
    private final String actor;
    private final JComboBox<String> type = new JComboBox<String>(new String[] { "TA Info", "Jobs", "Applications", "Results", "All" });
    private final JComboBox<String> format = new JComboBox<String>(new String[] { "CSV", "JSON", "XML" });

    private final JTextArea preview = new JTextArea();

    public AdminExportPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Data Export"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Type:"));
        left.add(type);
        left.add(new JLabel("Format:"));
        left.add(format);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton export = new JButton("Export" );
        actions.add(export);
        top.add(actions, BorderLayout.EAST);

        export.addActionListener(e -> export());

        type.addActionListener(e -> refreshPreview());
        format.addActionListener(e -> refreshPreview());

        add(top, BorderLayout.NORTH);

        preview.setEditable(false);
        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(new JLabel("Preview (shows first ~50 lines of what will be exported)"), BorderLayout.NORTH);
        center.add(new JScrollPane(preview), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);

        refreshPreview();
    }

    private void export() {
        data.reload();
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        Path dir = chooser.getSelectedFile().toPath();
        String t = String.valueOf(type.getSelectedItem());
        String f = String.valueOf(format.getSelectedItem());
        try {
            if ("All".equals(t)) {
                exportOne(dir, "TA Info", f);
                exportOne(dir, "Jobs", f);
                exportOne(dir, "Applications", f);
                exportOne(dir, "Results", f);
            } else {
                exportOne(dir, t, f);
            }
            OperationLog.append(data.tempOperationFile(), "INFO", "actor=" + actor + " action=export type=" + t + " format=" + f + " dir=" + dir.toAbsolutePath());
            JOptionPane.showMessageDialog(this, "Exported" );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed" );
        }
    }

    private void refreshPreview() {
        if (data == null) {
            preview.setText("");
            return;
        }
        try {
            data.reload();
            String t = String.valueOf(type.getSelectedItem());
            String f = String.valueOf(format.getSelectedItem());
            String text;
            if ("All".equals(t)) {
                StringBuilder sb = new StringBuilder();
                sb.append(previewFor("TA Info", f)).append("\n\n");
                sb.append(previewFor("Jobs", f)).append("\n\n");
                sb.append(previewFor("Applications", f)).append("\n\n");
                sb.append(previewFor("Results", f));
                text = sb.toString();
            } else {
                text = previewFor(t, f);
            }
            preview.setText(limitLines(text, 50));
            preview.setCaretPosition(0);
        } catch (RuntimeException ex) {
            preview.setText("Preview failed");
        }
    }

    private String previewFor(String t, String f) {
        if ("TA Info".equals(t)) {
            return "TA Info count=" + data.listApplicants().size() + "\n" + previewTa(f);
        }
        if ("Jobs".equals(t)) {
            return "Jobs count=" + data.listJobs().size() + "\n" + previewJobs(f);
        }
        if ("Applications".equals(t)) {
            int cnt = 0;
            for (Job j : data.listJobs()) cnt += data.listApplicationsForJob(j.id()).size();
            return "Applications count=" + cnt + "\n" + previewApplications(f);
        }
        if ("Results".equals(t)) {
            int cnt = 0;
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    if (a.status() == Application.Status.ACCEPTED) cnt++;
                }
            }
            return "Results (ACCEPTED) count=" + cnt + "\n" + previewResults(f);
        }
        return "";
    }

    private String previewTa(String format) {
        if ("CSV".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,name,email,skills,cvPath\n");
            int n = 0;
            for (Applicant a : data.listApplicants()) {
                sb.append(Csv.join(a.id(), a.name(), a.email(), a.skills(), a.cvPath())).append("\n");
                if (++n >= 10) break;
            }
            return sb.toString();
        }
        if ("JSON".equalsIgnoreCase(format)) {
            Map<String, Object> root = new LinkedHashMap<String, Object>();
            List<Object> arr = new LinkedList<Object>();
            int n = 0;
            for (Applicant a : data.listApplicants()) {
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("id", a.id());
                m.put("name", a.name());
                m.put("email", a.email());
                arr.add(m);
                if (++n >= 10) break;
            }
            root.put("tas_sample", arr);
            return MiniJson.stringify(root);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<tas>\n");
        int n = 0;
        for (Applicant a : data.listApplicants()) {
            sb.append("  <ta id=\"").append(escapeXml(a.id())).append("\" name=\"").append(escapeXml(a.name()))
                    .append("\" email=\"").append(escapeXml(a.email())).append("\"/>\n");
            if (++n >= 10) break;
        }
        sb.append("</tas>");
        return sb.toString();
    }

    private String previewJobs(String format) {
        if ("CSV".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,title,requiredSkills,hoursPerWeek,postedBy,status,category\n");
            int n = 0;
            for (Job j : data.listJobs()) {
                sb.append(Csv.join(j.id(), j.title(), j.requiredSkills(), String.valueOf(j.hoursPerWeek()), j.postedBy(), j.status().name(), j.category())).append("\n");
                if (++n >= 10) break;
            }
            return sb.toString();
        }
        if ("JSON".equalsIgnoreCase(format)) {
            Map<String, Object> root = new LinkedHashMap<String, Object>();
            List<Object> arr = new LinkedList<Object>();
            int n = 0;
            for (Job j : data.listJobs()) {
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("id", j.id());
                m.put("title", j.title());
                m.put("postedBy", j.postedBy());
                m.put("status", j.status().name());
                m.put("category", j.category());
                arr.add(m);
                if (++n >= 10) break;
            }
            root.put("jobs_sample", arr);
            return MiniJson.stringify(root);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<jobs>\n");
        int n = 0;
        for (Job j : data.listJobs()) {
            sb.append("  <job id=\"").append(escapeXml(j.id())).append("\" title=\"").append(escapeXml(j.title())).append("\"/>\n");
            if (++n >= 10) break;
        }
        sb.append("</jobs>");
        return sb.toString();
    }

    private String previewApplications(String format) {
        if ("CSV".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("id,applicantId,jobId,status,createdAt\n");
            int n = 0;
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    sb.append(Csv.join(a.id(), a.applicantId(), a.jobId(), a.status().name(), String.valueOf(a.createdAt()))).append("\n");
                    if (++n >= 10) return sb.toString();
                }
            }
            return sb.toString();
        }
        if ("JSON".equalsIgnoreCase(format)) {
            Map<String, Object> root = new LinkedHashMap<String, Object>();
            List<Object> arr = new LinkedList<Object>();
            int n = 0;
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("id", a.id());
                    m.put("applicantId", a.applicantId());
                    m.put("jobId", a.jobId());
                    m.put("status", a.status().name());
                    m.put("createdAt", Long.valueOf(a.createdAt()));
                    arr.add(m);
                    if (++n >= 10) break;
                }
                if (n >= 10) break;
            }
            root.put("applications_sample", arr);
            return MiniJson.stringify(root);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<applications>\n");
        int n = 0;
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                sb.append("  <application id=\"").append(escapeXml(a.id())).append("\" applicantId=\"").append(escapeXml(a.applicantId()))
                        .append("\" jobId=\"").append(escapeXml(a.jobId())).append("\"/>\n");
                if (++n >= 10) return sb.append("</applications>").toString();
            }
        }
        sb.append("</applications>");
        return sb.toString();
    }

    private String previewResults(String format) {
        if ("CSV".equalsIgnoreCase(format)) {
            StringBuilder sb = new StringBuilder();
            sb.append("applicationId,applicantId,jobId,jobTitle,postedBy,hoursPerWeek,category,createdAt\n");
            int n = 0;
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    if (a.status() != Application.Status.ACCEPTED) continue;
                    sb.append(Csv.join(a.id(), a.applicantId(), a.jobId(), j.title(), j.postedBy(), String.valueOf(j.hoursPerWeek()), j.category(), String.valueOf(a.createdAt()))).append("\n");
                    if (++n >= 10) return sb.toString();
                }
            }
            return sb.toString();
        }
        if ("JSON".equalsIgnoreCase(format)) {
            Map<String, Object> root = new LinkedHashMap<String, Object>();
            List<Object> arr = new LinkedList<Object>();
            int n = 0;
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    if (a.status() != Application.Status.ACCEPTED) continue;
                    Map<String, Object> m = new LinkedHashMap<String, Object>();
                    m.put("applicationId", a.id());
                    m.put("applicantId", a.applicantId());
                    m.put("jobId", a.jobId());
                    m.put("jobTitle", j.title());
                    arr.add(m);
                    if (++n >= 10) break;
                }
                if (n >= 10) break;
            }
            root.put("results_sample", arr);
            return MiniJson.stringify(root);
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<results>\n");
        int n = 0;
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                sb.append("  <result applicationId=\"").append(escapeXml(a.id())).append("\" applicantId=\"").append(escapeXml(a.applicantId()))
                        .append("\" jobId=\"").append(escapeXml(a.jobId())).append("\"/>\n");
                if (++n >= 10) return sb.append("</results>").toString();
            }
        }
        sb.append("</results>");
        return sb.toString();
    }

    private static String limitLines(String text, int maxLines) {
        if (text == null) return "";
        String[] lines = text.split("\\r?\\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length && i < maxLines; i++) {
            sb.append(lines[i]).append("\n");
        }
        return sb.toString();
    }

    private void exportOne(Path dir, String type, String format) throws IOException {
        if ("TA Info".equals(type)) {
            if ("CSV".equalsIgnoreCase(format)) {
                exportTaCsv(dir.resolve("ta_info.csv"));
            } else if ("JSON".equalsIgnoreCase(format)) {
                exportTaJson(dir.resolve("ta_info.json"));
            } else {
                exportTaXml(dir.resolve("ta_info.xml"));
            }
            return;
        }
        if ("Jobs".equals(type)) {
            if ("CSV".equalsIgnoreCase(format)) {
                exportJobsCsv(dir.resolve("jobs.csv"));
            } else if ("JSON".equalsIgnoreCase(format)) {
                exportJobsJson(dir.resolve("jobs.json"));
            } else {
                exportJobsXml(dir.resolve("jobs.xml"));
            }
            return;
        }
        if ("Applications".equals(type)) {
            if ("CSV".equalsIgnoreCase(format)) {
                exportApplicationsCsv(dir.resolve("applications.csv"));
            } else if ("JSON".equalsIgnoreCase(format)) {
                exportApplicationsJson(dir.resolve("applications.json"));
            } else {
                exportApplicationsXml(dir.resolve("applications.xml"));
            }
            return;
        }
        if ("Results".equals(type)) {
            if ("CSV".equalsIgnoreCase(format)) {
                exportResultsCsv(dir.resolve("results.csv"));
            } else if ("JSON".equalsIgnoreCase(format)) {
                exportResultsJson(dir.resolve("results.json"));
            } else {
                exportResultsXml(dir.resolve("results.xml"));
            }
        }
    }

    private void exportTaCsv(Path out) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("id,name,email,skills,cvPath");
        for (Applicant a : data.listApplicants()) {
            lines.add(Csv.join(a.id(), a.name(), a.email(), a.skills(), a.cvPath()));
        }
        Files.write(out, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportTaJson(Path out) throws IOException {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        List<Object> arr = new LinkedList<Object>();
        for (Applicant a : data.listApplicants()) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("id", a.id());
            m.put("name", a.name());
            m.put("email", a.email());
            m.put("skills", a.skills());
            m.put("cvPath", a.cvPath());
            arr.add(m);
        }
        root.put("tas", arr);
        Files.write(out, MiniJson.stringify(root).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportTaXml(Path out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<tas>\n");
        for (Applicant a : data.listApplicants()) {
            sb.append("  <ta id=\"").append(escapeXml(a.id())).append("\" name=\"").append(escapeXml(a.name()))
                    .append("\" email=\"").append(escapeXml(a.email())).append("\" skills=\"").append(escapeXml(a.skills()))
                    .append("\" cvPath=\"").append(escapeXml(a.cvPath())).append("\"/>\n");
        }
        sb.append("</tas>\n");
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportJobsCsv(Path out) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("id,title,requiredSkills,hoursPerWeek,postedBy,status,category");
        for (Job j : data.listJobs()) {
            lines.add(Csv.join(j.id(), j.title(), j.requiredSkills(), String.valueOf(j.hoursPerWeek()), j.postedBy(), j.status().name(), j.category()));
        }
        Files.write(out, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportJobsJson(Path out) throws IOException {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        List<Object> arr = new LinkedList<Object>();
        for (Job j : data.listJobs()) {
            Map<String, Object> m = new LinkedHashMap<String, Object>();
            m.put("id", j.id());
            m.put("title", j.title());
            m.put("description", j.description());
            m.put("requiredSkills", j.requiredSkills());
            m.put("hoursPerWeek", Integer.valueOf(j.hoursPerWeek()));
            m.put("postedBy", j.postedBy());
            m.put("status", j.status().name());
            m.put("category", j.category());
            arr.add(m);
        }
        root.put("jobs", arr);
        Files.write(out, MiniJson.stringify(root).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportJobsXml(Path out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<jobs>\n");
        for (Job j : data.listJobs()) {
            sb.append("  <job id=\"").append(escapeXml(j.id())).append("\" title=\"").append(escapeXml(j.title()))
                    .append("\" requiredSkills=\"").append(escapeXml(j.requiredSkills())).append("\" hoursPerWeek=\"").append(j.hoursPerWeek())
                    .append("\" postedBy=\"").append(escapeXml(j.postedBy())).append("\" status=\"").append(escapeXml(j.status().name()))
                    .append("\" category=\"").append(escapeXml(j.category())).append("\"/>");
            sb.append("\n");
        }
        sb.append("</jobs>\n");
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportApplicationsCsv(Path out) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("id,applicantId,jobId,status,createdAt");
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                lines.add(Csv.join(a.id(), a.applicantId(), a.jobId(), a.status().name(), String.valueOf(a.createdAt())));
            }
        }
        Files.write(out, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportApplicationsJson(Path out) throws IOException {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        List<Object> arr = new LinkedList<Object>();
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("id", a.id());
                m.put("applicantId", a.applicantId());
                m.put("jobId", a.jobId());
                m.put("status", a.status().name());
                m.put("createdAt", Long.valueOf(a.createdAt()));
                arr.add(m);
            }
        }
        root.put("applications", arr);
        Files.write(out, MiniJson.stringify(root).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportApplicationsXml(Path out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<applications>\n");
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                sb.append("  <application id=\"").append(escapeXml(a.id())).append("\" applicantId=\"").append(escapeXml(a.applicantId()))
                        .append("\" jobId=\"").append(escapeXml(a.jobId())).append("\" status=\"").append(escapeXml(a.status().name()))
                        .append("\" createdAt=\"").append(a.createdAt()).append("\"/>\n");
            }
        }
        sb.append("</applications>\n");
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportResultsCsv(Path out) throws IOException {
        List<String> lines = new ArrayList<String>();
        lines.add("applicationId,applicantId,jobId,jobTitle,postedBy,hoursPerWeek,category,createdAt");
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                lines.add(Csv.join(a.id(), a.applicantId(), a.jobId(), j.title(), j.postedBy(), String.valueOf(j.hoursPerWeek()), j.category(), String.valueOf(a.createdAt())));
            }
        }
        Files.write(out, lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportResultsJson(Path out) throws IOException {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        List<Object> arr = new LinkedList<Object>();
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                Map<String, Object> m = new LinkedHashMap<String, Object>();
                m.put("applicationId", a.id());
                m.put("applicantId", a.applicantId());
                m.put("jobId", a.jobId());
                m.put("jobTitle", j.title());
                m.put("postedBy", j.postedBy());
                m.put("hoursPerWeek", Integer.valueOf(j.hoursPerWeek()));
                m.put("category", j.category());
                m.put("createdAt", Long.valueOf(a.createdAt()));
                arr.add(m);
            }
        }
        root.put("results", arr);
        Files.write(out, MiniJson.stringify(root).getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private void exportResultsXml(Path out) throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("<results>\n");
        for (Job j : data.listJobs()) {
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                sb.append("  <result applicationId=\"").append(escapeXml(a.id())).append("\" applicantId=\"").append(escapeXml(a.applicantId()))
                        .append("\" jobId=\"").append(escapeXml(a.jobId())).append("\" jobTitle=\"").append(escapeXml(j.title()))
                        .append("\" postedBy=\"").append(escapeXml(j.postedBy())).append("\" hoursPerWeek=\"").append(j.hoursPerWeek())
                        .append("\" category=\"").append(escapeXml(j.category())).append("\" createdAt=\"").append(a.createdAt()).append("\"/>\n");
            }
        }
        sb.append("</results>\n");
        Files.write(out, sb.toString().getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }

    private static String escapeXml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }
}
