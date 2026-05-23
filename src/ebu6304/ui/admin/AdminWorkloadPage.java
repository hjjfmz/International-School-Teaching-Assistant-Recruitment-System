package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.storage.MiniJson;
import ebu6304.storage.OperationLog;
import ebu6304.ui.I18n;
import ebu6304.ui.UiTheme;

public final class AdminWorkloadPage extends JPanel {

    private enum WorkloadStatus { OVERLOADED, NORMAL, UNDERLOADED, IDLE }

    private static final int OVERLOAD_HOURS = 20;
    private static final String AI_ENDPOINT = "https://token-plan-cn.xiaomimimo.com/v1/chat/completions";
    private static final String AI_KEY      = "tp-cod47ehotbktykmfmeb02oxkxt777874ern2bfslnjazju1m";
    private static final String AI_MODEL    = "mimo-v2.5-pro";

    private final DataService data;
    private final String actor;

    private final JComboBox<String> view = new JComboBox<String>(new String[] { "By TA", "By Job" });
    private final JTextField from = new JTextField(10);
    private final JTextField to = new JTextField(10);
    private final JTextField category = new JTextField(10);

    private final DefaultTableModel model;
    private final JTable table;
    private final List<WorkloadStatus> rowStatuses = new ArrayList<>();
    private final List<Color> rowColors = new ArrayList<>();

    public AdminWorkloadPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        UiTheme.stylePage(this);

        model = new DefaultTableModel(new Object[] { I18n.t("admin.workload.col.key"), I18n.t("admin.workload.col.name"), I18n.t("admin.workload.col.accepted"), I18n.t("admin.workload.col.hours") }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (!isRowSelected(row) && row < rowColors.size()) {
                    c.setBackground(rowColors.get(row));
                    if (c instanceof javax.swing.JComponent) ((javax.swing.JComponent) c).setOpaque(true);
                }
                return c;
            }
        };
        UiTheme.styleTable(table);
        UiTheme.styleCombo(view);
        UiTheme.styleTextField(from);
        UiTheme.styleTextField(to);
        UiTheme.styleTextField(category);
        from.setPreferredSize(new java.awt.Dimension(116, 36));
        to.setPreferredSize(new java.awt.Dimension(116, 36));
        category.setPreferredSize(new java.awt.Dimension(132, 36));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        UiTheme.stylePanelCard(top, I18n.t("admin.workload.title"));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.add(new JLabel(I18n.t("admin.workload.view")));
        filters.add(view);
        filters.add(new JLabel(I18n.t("admin.workload.from")));
        filters.add(from);
        filters.add(new JLabel(I18n.t("admin.workload.to")));
        filters.add(to);
        filters.add(new JLabel(I18n.t("admin.workload.category")));
        filters.add(category);
        top.add(filters, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton export = new JButton(I18n.t("common.exportcsv"));
        JButton ai = new JButton(I18n.t("admin.workload.ai"));
        UiTheme.styleButton(refresh);
        UiTheme.styleButton(export);
        UiTheme.stylePrimaryButton(ai);
        actions.add(refresh);
        actions.add(export);
        actions.add(ai);
        top.add(actions, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refresh());
        export.addActionListener(e -> exportCsv());
        ai.addActionListener(e -> doAiAnalysis(ai));

        view.addActionListener(e -> refresh());
        from.addActionListener(e -> refresh());
        to.addActionListener(e -> refresh());
        category.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        UiTheme.styleScrollPane(sp);
        add(sp, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        rowStatuses.clear();
        rowColors.clear();
        if (data == null) return;

        long fromMs = parseDateStart(from.getText().trim());
        long toMs = parseDateEnd(to.getText().trim());
        String catFilter = category.getText().trim().toLowerCase();

        String v = String.valueOf(view.getSelectedItem());
        if ("By Job".equalsIgnoreCase(v)) {
            refreshByJob(fromMs, toMs, catFilter);
        } else {
            refreshByTa(fromMs, toMs, catFilter);
        }
    }

    private static final class TaRow {
        final String id, name;
        final int accepted, hours, submitted;
        TaRow(String id, String name, int accepted, int hours, int submitted) {
            this.id = id; this.name = name;
            this.accepted = accepted; this.hours = hours; this.submitted = submitted;
        }
    }

    private void refreshByTa(long fromMs, long toMs, String catFilter) {
        List<TaRow> rows = new ArrayList<>();
        for (Applicant ta : data.listApplicants()) {
            int accepted = 0, hours = 0, submitted = 0;
            for (Application a : data.listApplicationsForApplicant(ta.id())) {
                if (!within(a.createdAt(), fromMs, toMs)) continue;
                Job j = data.getJob(a.jobId()).orElse(null);
                if (j == null) continue;
                if (!catFilter.isEmpty() && !j.category().toLowerCase().contains(catFilter)) continue;
                if (a.status() == Application.Status.ACCEPTED) { accepted++; hours += j.hoursPerWeek(); }
                else if (a.status() == Application.Status.SUBMITTED) { submitted++; }
            }
            rows.add(new TaRow(ta.id(), ta.name(), accepted, hours, submitted));
        }
        rows.sort((a, b) -> b.hours - a.hours);
        int maxH = rows.isEmpty() ? 0 : rows.get(0).hours;
        for (TaRow r : rows) {
            WorkloadStatus st;
            if (r.hours > OVERLOAD_HOURS)       st = WorkloadStatus.OVERLOADED;
            else if (r.hours > 0)               st = WorkloadStatus.NORMAL;
            else if (r.submitted > 0)           st = WorkloadStatus.UNDERLOADED;
            else                                st = WorkloadStatus.IDLE;
            rowStatuses.add(st);
            rowColors.add(workloadColor(r.hours, maxH));
            model.addRow(new Object[]{ r.id, r.name, r.accepted, r.hours });
        }
    }

    private static Color workloadColor(int hours, int maxHours) {
        if (maxHours == 0) return Color.getHSBColor(0.33f, 0.20f, 0.97f);
        float relRatio = (float) hours / maxHours;
        float absRatio = Math.min((float) hours / OVERLOAD_HOURS, 1.0f);
        float hue = 0.33f * (1f - relRatio);
        float sat = absRatio * 0.65f + 0.15f;
        return Color.getHSBColor(hue, sat, 0.97f);
    }

    private void refreshByJob(long fromMs, long toMs, String catFilter) {
        List<Job> jobs = data.listJobs();
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.title(), o2.title());
            }
        });

        List<int[]> hoursPerRow = new ArrayList<>();
        for (Job j : jobs) {
            if (!catFilter.isEmpty() && !j.category().toLowerCase().contains(catFilter)) continue;
            int accepted = 0;
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                if (!within(a.createdAt(), fromMs, toMs)) continue;
                accepted++;
            }
            int hours = accepted * j.hoursPerWeek();
            hoursPerRow.add(new int[]{hours});
            model.addRow(new Object[] { j.id(), j.title(), Integer.valueOf(accepted), Integer.valueOf(hours) });
        }
        int maxH = hoursPerRow.stream().mapToInt(h -> h[0]).max().orElse(0);
        for (int[] h : hoursPerRow) {
            rowColors.add(workloadColor(h[0], maxH));
            if (h[0] > OVERLOAD_HOURS)  rowStatuses.add(WorkloadStatus.OVERLOADED);
            else if (h[0] > 0)          rowStatuses.add(WorkloadStatus.NORMAL);
            else                        rowStatuses.add(WorkloadStatus.IDLE);
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.t("admin.workload.export.title"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        List<String> lines = new ArrayList<String>();
        lines.add("key,nameOrTitle,accepted,totalHours");
        for (int i = 0; i < model.getRowCount(); i++) {
            lines.add(ebu6304.storage.Csv.join(
                    String.valueOf(model.getValueAt(i, 0)),
                    String.valueOf(model.getValueAt(i, 1)),
                    String.valueOf(model.getValueAt(i, 2)),
                    String.valueOf(model.getValueAt(i, 3))
            ));
        }

        try {
            Files.write(chooser.getSelectedFile().toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            OperationLog.append(data.tempOperationFile(), "INFO", "actor=" + actor + " action=exportWorkloadCsv file=" + chooser.getSelectedFile().getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.export.failed"));
        }
    }

    private void doAiAnalysis(JButton trigger) {
        if (data == null) return;
        StringBuilder sb = new StringBuilder();
        sb.append("You are an AI assistant for the BUPT TA Recruitment System admin.\n");
        sb.append("Analyze the following TA workload data. Overload threshold = ").append(OVERLOAD_HOURS).append(" hours/week.\n");
        sb.append("For each TA state: current status (OVERLOADED/NORMAL/UNDERLOADED/IDLE) and a brief recommendation.\n");
        sb.append("DO NOT modify any records — this is analysis only.\n\n");
        sb.append("=== WORKLOAD DATA ===\n");
        for (int i = 0; i < model.getRowCount(); i++) {
            String status = i < rowStatuses.size() ? rowStatuses.get(i).name() : "?";
            sb.append(String.format("  %-20s %-20s accepted=%s hours=%s status=%s%n",
                model.getValueAt(i, 0), model.getValueAt(i, 1),
                model.getValueAt(i, 2), model.getValueAt(i, 3), status));
        }
        sb.append("\nProvide a concise analysis with: 1) Summary table 2) Overloaded TAs to review 3) Underloaded/Idle TAs who can take more work 4) Balancing suggestions.");
        String prompt = sb.toString();

        trigger.setEnabled(false);
        trigger.setText("AI 分析中…");
        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return callAiApi(prompt);
            }
            @Override protected void done() {
                trigger.setEnabled(true);
                trigger.setText(I18n.t("admin.workload.ai"));
                try {
                    String result = get();
                    if (result == null || result.trim().isEmpty()) {
                        result = "AI 未返回内容，请确认 API Key 和网络后重试。";
                    }
                    JTextPane pane = new JTextPane();
                    pane.setContentType("text/html");
                    pane.setEditable(false);
                    pane.setBackground(Color.WHITE);
                    String html = "<html><body style='font-family:Dialog,sans-serif;font-size:13px;margin:10px;'>" + mdToHtml(result) + "</body></html>";
                    pane.setText(html);
                    pane.setCaretPosition(0);
                    JScrollPane sp2 = new JScrollPane(pane);
                    sp2.setPreferredSize(new java.awt.Dimension(760, 540));
                    JOptionPane.showMessageDialog(AdminWorkloadPage.this,
                        sp2, "AI 工作量分析（仅供参考，不修改任何数据）",
                        JOptionPane.INFORMATION_MESSAGE);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(AdminWorkloadPage.this,
                        "AI 分析失败: " + ex.getMessage());
                }
            }
        }.execute();
    }

    @SuppressWarnings("unchecked")
    private String callAiApi(String userContent) throws Exception {
        Map<String, Object> msg = new LinkedHashMap<>();
        msg.put("role", "user"); msg.put("content", userContent);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", AI_MODEL);
        body.put("messages", java.util.Arrays.asList(msg));
        body.put("max_tokens", 2048);
        byte[] payload = MiniJson.stringify(body).getBytes(StandardCharsets.UTF_8);
        URL url = new java.net.URI(AI_ENDPOINT).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + AI_KEY);
        conn.setConnectTimeout(15000); conn.setReadTimeout(90000); conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload); }
        int code = conn.getResponseCode();
        InputStream is = code < 300 ? conn.getInputStream() : conn.getErrorStream();
        byte[] buf = is.readAllBytes();
        String resp = new String(buf, StandardCharsets.UTF_8);
        Object root = MiniJson.parse(resp);
        if (root instanceof Map) {
            Object choices = ((Map<String,Object>) root).get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object first = ((List<?>) choices).get(0);
                if (first instanceof Map) {
                    Object m2 = ((Map<String,Object>) first).get("message");
                    if (m2 instanceof Map) { Object c = ((Map<String,Object>) m2).get("content"); if (c != null) return c.toString(); }
                }
            }
        }
        throw new IOException("Unexpected API response");
    }

    private static String mdToHtml(String md) {
        if (md == null) return "";
        String[] lines = md.split("\n", -1);
        StringBuilder out = new StringBuilder();
        boolean inUl = false; boolean inOl = false; boolean inTable = false;
        for (String raw : lines) {
            String esc = raw.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
            boolean isTableRow = raw.trim().startsWith("|");
            boolean isSep      = raw.matches("\\s*\\|[-| :]+\\|");
            boolean isBullet   = raw.matches("^\\s*[-*] .+");
            boolean isNum      = raw.matches("^\\s*\\d+[.)]\\ .+");
            boolean isHr       = raw.trim().matches("[-*_]{3,}");
            if (!isTableRow && inTable) { out.append("</table>"); inTable = false; }
            if (!isBullet   && inUl)   { out.append("</ul>");    inUl    = false; }
            if (!isNum      && inOl)   { out.append("</ol>");    inOl    = false; }
            if (isTableRow) {
                if (isSep) continue;
                if (!inTable) { out.append("<table border='0' cellpadding='5' cellspacing='0' style='border-collapse:collapse;margin:6px 0;width:auto'>"); inTable = true; }
                String[] cells = raw.split("\\|", -1);
                out.append("<tr>");
                for (int ci = 1; ci < cells.length - 1; ci++) {
                    out.append("<td style='border:1px solid #ccc;padding:4px 8px;'>").append(inline(cells[ci].trim())).append("</td>");
                }
                out.append("</tr>"); continue;
            }
            if (isHr)  { out.append("<hr style='border:none;border-top:1px solid #ddd;margin:8px 0'>"); continue; }
            if (esc.startsWith("##### ")) { out.append("<h5 style='margin:4px 0'>").append(inline(raw.substring(6))).append("</h5>"); continue; }
            if (esc.startsWith("#### "))  { out.append("<h4 style='margin:4px 0'>").append(inline(raw.substring(5))).append("</h4>"); continue; }
            if (esc.startsWith("### "))   { out.append("<h4 style='margin:4px 0'>").append(inline(raw.substring(4))).append("</h4>"); continue; }
            if (esc.startsWith("## "))    { out.append("<h3 style='margin:4px 0'>").append(inline(raw.substring(3))).append("</h3>"); continue; }
            if (esc.startsWith("# "))     { out.append("<h3 style='margin:4px 0'>").append(inline(raw.substring(2))).append("</h3>"); continue; }
            if (isBullet) {
                if (!inUl) { out.append("<ul style='margin:2px 0;padding-left:20px'>"); inUl = true; }
                out.append("<li>").append(inline(raw.replaceFirst("^\\s*[-*] ", ""))).append("</li>"); continue;
            }
            if (isNum) {
                if (!inOl) { out.append("<ol style='margin:2px 0;padding-left:20px'>"); inOl = true; }
                out.append("<li>").append(inline(raw.replaceFirst("^\\s*\\d+[.)]\\ ", ""))).append("</li>"); continue;
            }
            if (raw.trim().isEmpty()) { out.append("<br>"); continue; }
            out.append("<p style='margin:2px 0'>").append(inline(raw)).append("</p>");
        }
        if (inUl) out.append("</ul>");
        if (inOl) out.append("</ol>");
        if (inTable) out.append("</table>");
        return out.toString();
    }

    private static String inline(String t) {
        t = t.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
        t = t.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        t = t.replaceAll("\\*(.+?)\\*",       "<i>$1</i>");
        t = t.replaceAll("`(.+?)`",            "<code style='background:#f0f0f0'>$1</code>");
        return t;
    }

    private static boolean within(long t, long fromMs, long toMs) {
        if (fromMs > 0 && t < fromMs) return false;
        if (toMs > 0 && t > toMs) return false;
        return true;
    }

    private static long parseDateStart(String text) {
        if (text == null || text.trim().isEmpty()) return 0L;
        try {
            LocalDate d = LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException ex) {
            return 0L;
        }
    }

    private static long parseDateEnd(String text) {
        if (text == null || text.trim().isEmpty()) return 0L;
        try {
            LocalDate d = LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return d.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        } catch (DateTimeParseException ex) {
            return 0L;
        }
    }
}


