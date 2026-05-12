package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
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
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.Csv;
import ebu6304.storage.DataService;
import ebu6304.storage.MiniJson;
import ebu6304.storage.OperationLog;
import ebu6304.ui.I18n;

public final class AdminExportPage extends JPanel {
    private final DataService data;
    private final String actor;
    private final JComboBox<String> type = new JComboBox<String>(new String[] { "TA Info", "Jobs", "Applications", "Results", "All" });
    private final JComboBox<String> format = new JComboBox<String>(new String[] { "CSV", "JSON", "XML" });

    private final JPanel cardsList = new JPanel();
    private final JButton prevBtn = new JButton("Prev");
    private final JButton nextBtn = new JButton("Next");
    private final JLabel pageLabel = new JLabel();
    private final JComboBox<Integer> pageSizeBox = new JComboBox<Integer>(new Integer[] { 5, 10, 20, 50 });

    private int page = 1;
    private int totalPages = 1;
    private int totalItems = 0;

    public AdminExportPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("admin.export.title")));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel(I18n.t("admin.export.type")));
        left.add(type);
        left.add(new JLabel(I18n.t("admin.export.format")));
        left.add(format);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton export = new JButton(I18n.t("common.export"));
        actions.add(export);
        top.add(actions, BorderLayout.EAST);

        export.addActionListener(e -> export());

        add(top, BorderLayout.NORTH);

        cardsList.setOpaque(false);
        cardsList.setLayout(new BoxLayout(cardsList, BoxLayout.Y_AXIS));

        JPanel center = new JPanel(new BorderLayout(6, 6));
        center.add(new JLabel(I18n.t("admin.export.preview")), BorderLayout.NORTH);
        JScrollPane cardsSp = new JScrollPane(cardsList);
        cardsSp.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pager.add(new JLabel("Page size"));
        pager.add(pageSizeBox);
        pager.add(prevBtn);
        pager.add(pageLabel);
        pager.add(nextBtn);

        center.add(cardsSp, BorderLayout.CENTER);
        center.add(pager, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        pageSizeBox.setSelectedItem(Integer.valueOf(10));
        pageSizeBox.addActionListener(e -> {
            page = 1;
            refreshCardsOnly();
        });
        prevBtn.addActionListener(e -> {
            if (page > 1) {
                page--;
                refreshCardsOnly();
            }
        });
        nextBtn.addActionListener(e -> {
            if (page < totalPages) {
                page++;
                refreshCardsOnly();
            }
        });

        type.addActionListener(e -> {
            page = 1;
            refreshPreview();
        });
        format.addActionListener(e -> {
            page = 1;
            refreshPreview();
        });

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
            JOptionPane.showMessageDialog(this, I18n.t("msg.export.success"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.export.failed"));
        }
    }

    private void refreshPreview() {
        if (data == null) {
            cardsList.removeAll();
            cardsList.revalidate();
            cardsList.repaint();
            return;
        }
        try {
            data.reload();
            refreshCardsOnly();
        } catch (RuntimeException ex) {
            cardsList.removeAll();
            cardsList.add(new JLabel("Preview failed"));
            cardsList.revalidate();
            cardsList.repaint();
        }
    }

    private void refreshCardsOnly() {
        String t = String.valueOf(type.getSelectedItem());
        List<CardItem> items = buildItems(t);
        totalItems = items.size();

        int pageSize = ((Integer) pageSizeBox.getSelectedItem()).intValue();
        totalPages = Math.max(1, (int) Math.ceil((double) totalItems / (double) pageSize));
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;

        int from = (page - 1) * pageSize;
        int to = Math.min(totalItems, from + pageSize);

        cardsList.removeAll();
        cardsList.add(Box.createVerticalStrut(4));

        if (totalItems == 0) {
            JLabel empty = new JLabel("(No data)");
            empty.setForeground(new Color(140, 140, 140));
            empty.setAlignmentX(0f);
            cardsList.add(empty);
        } else {
            for (int i = from; i < to; i++) {
                CardItem it = items.get(i);
                JPanel c = card(it.title, it.fields);
                c.setAlignmentX(0f);
                cardsList.add(c);
                cardsList.add(Box.createVerticalStrut(10));
            }
        }

        cardsList.add(Box.createVerticalGlue());
        cardsList.revalidate();
        cardsList.repaint();

        pageLabel.setText("Page " + page + "/" + totalPages + " (" + totalItems + ")");
        prevBtn.setEnabled(page > 1);
        nextBtn.setEnabled(page < totalPages);
    }

    private List<CardItem> buildItems(String t) {
        List<CardItem> out = new ArrayList<CardItem>();
        if ("All".equals(t)) {
            out.addAll(buildItems("TA Info"));
            out.addAll(buildItems("Jobs"));
            out.addAll(buildItems("Applications"));
            out.addAll(buildItems("Results"));
            return out;
        }

        if ("TA Info".equals(t)) {
            for (Applicant a : data.listApplicants()) {
                out.add(new CardItem("TA: " + safe(a.id()), new String[][] {
                        { "Name", safe(a.name()) },
                        { "Email", safe(a.email()) },
                        { "Skills", safe(a.skills()) },
                        { "CV", safe(a.cvPath()) },
                }));
            }
            return out;
        }
        if ("Jobs".equals(t)) {
            for (Job j : data.listJobs()) {
                out.add(new CardItem("Job: " + safe(j.id()), new String[][] {
                        { "Title", safe(j.title()) },
                        { "PostedBy", safe(j.postedBy()) },
                        { "Status", j.status() == null ? "" : j.status().name() },
                        { "Category", safe(j.category()) },
                        { "Hours/Week", String.valueOf(j.hoursPerWeek()) },
                        { "Skills", safe(j.requiredSkills()) },
                }));
            }
            return out;
        }
        if ("Applications".equals(t)) {
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    out.add(new CardItem("Application: " + safe(a.id()), new String[][] {
                            { "Applicant", safe(a.applicantId()) },
                            { "Job", safe(a.jobId()) },
                            { "Status", a.status() == null ? "" : a.status().name() },
                            { "CreatedAt", String.valueOf(a.createdAt()) },
                    }));
                }
            }
            return out;
        }
        if ("Results".equals(t)) {
            for (Job j : data.listJobs()) {
                for (Application a : data.listApplicationsForJob(j.id())) {
                    if (a.status() != Application.Status.ACCEPTED) continue;
                    out.add(new CardItem("Result: " + safe(a.id()), new String[][] {
                            { "Applicant", safe(a.applicantId()) },
                            { "Job", safe(a.jobId()) },
                            { "Job Title", safe(j.title()) },
                            { "PostedBy", safe(j.postedBy()) },
                            { "Category", safe(j.category()) },
                            { "CreatedAt", String.valueOf(a.createdAt()) },
                    }));
                }
            }
            return out;
        }
        return out;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private static final class CardItem {
        private final String title;
        private final String[][] fields;

        private CardItem(String title, String[][] fields) {
            this.title = title;
            this.fields = fields;
        }
    }

    private static JPanel card(String title, String[][] fields) {
        RoundedPanel card = new RoundedPanel(14);
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(230, 230, 230), 1),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        card.setLayout(new BorderLayout(0, 8));

        JLabel h = new JLabel(title);
        h.setFont(h.getFont().deriveFont(Font.BOLD, 13f));
        h.setForeground(new Color(40, 40, 40));
        card.add(h, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridBagLayout());
        grid.setOpaque(false);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.NORTHWEST;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.insets = new Insets(2, 0, 2, 0);

        for (String[] kv : fields) {
            String k = kv[0];
            String v = kv[1];

            JPanel row = new JPanel(new BorderLayout(10, 0));
            row.setOpaque(false);

            JLabel key = new JLabel(k);
            key.setForeground(new Color(120, 120, 120));
            key.setFont(key.getFont().deriveFont(Font.PLAIN, 12f));
            key.setPreferredSize(new Dimension(90, 16));

            JLabel val = new JLabel("<html>" + escapeHtml(v) + "</html>");
            val.setForeground(new Color(50, 50, 50));
            val.setFont(val.getFont().deriveFont(Font.PLAIN, 12f));

            row.add(key, BorderLayout.WEST);
            row.add(val, BorderLayout.CENTER);

            grid.add(row, gc);
            gc.gridy++;
        }

        card.add(grid, BorderLayout.CENTER);
        return card;
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class RoundedPanel extends JPanel {
        private final int arc;

        private RoundedPanel(int arc) {
            super();
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
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
