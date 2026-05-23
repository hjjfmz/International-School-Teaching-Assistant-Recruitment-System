package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import ebu6304.ai.ResumeTextExtractor;
import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.storage.MiniJson;
import ebu6304.ui.I18n;
import ebu6304.ui.UiTheme;

public final class AdminAiPage extends JPanel {

    private static final String DEFAULT_ENDPOINT = "https://token-plan-cn.xiaomimimo.com/v1";
    private static final String DEFAULT_API_KEY  = "tp-cod47ehotbktykmfmeb02oxkxt777874ern2bfslnjazju1m";
    private static final String[] MODELS = {
        "mimo-v2.5-pro", "mimo-v2-pro", "mimo-v2.5", "mimo-v2-omni"
    };

    private final DataService data;
    private final List<Map<String, Object>> history = new ArrayList<>();
    private final List<String[]> chatMessages = new ArrayList<>();

    private final JTextPane chatPane = new JTextPane();
    private final JTextField inputField = new JTextField();
    private final JButton sendBtn = new JButton(I18n.t("admin.ai.send"));
    private final JButton clearBtn = new JButton(I18n.t("admin.ai.clear"));
    private final JButton lookupBtn = new JButton("\uD83D\uDD0D " + I18n.t("admin.ai.lookup"));
    private final JComboBox<String> modelBox = new JComboBox<>(MODELS);
    private final JTextField apiKeyField = new JTextField(DEFAULT_API_KEY, 28);
    private final JTextField endpointField = new JTextField(DEFAULT_ENDPOINT, 32);
    private final JLabel statusLabel = new JLabel(" ");

    public AdminAiPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        UiTheme.stylePage(this);

        JPanel top = new JPanel(new BorderLayout(10, 5));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setOpaque(false);
        toolbar.add(new JLabel(I18n.t("admin.ai.endpoint")));
        toolbar.add(endpointField);
        toolbar.add(new JLabel(I18n.t("admin.ai.apikey")));
        toolbar.add(apiKeyField);
        toolbar.add(new JLabel(I18n.t("admin.ai.model")));
        toolbar.add(modelBox);
        toolbar.add(clearBtn);
        toolbar.add(lookupBtn);
        top.setOpaque(false);
        top.add(toolbar, BorderLayout.CENTER);

        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        chatPane.setBackground(UiTheme.CARD_BG);
        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setPreferredSize(new Dimension(600, 420));
        UiTheme.styleScrollPane(chatScroll);

        statusLabel.setForeground(UiTheme.MUTED);
        JPanel bottom = new JPanel(new BorderLayout(6, 4));
        bottom.setOpaque(false);
        bottom.add(statusLabel, BorderLayout.NORTH);
        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        inputRow.setOpaque(false);
        sendBtn.setPreferredSize(new Dimension(80, sendBtn.getPreferredSize().height));
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);
        bottom.add(inputRow, BorderLayout.CENTER);

        UiTheme.styleTextField(inputField);
        UiTheme.styleTextField(endpointField);
        UiTheme.styleTextField(apiKeyField);
        UiTheme.styleCombo(modelBox);
        UiTheme.styleButton(clearBtn);
        UiTheme.styleButton(lookupBtn);
        UiTheme.stylePrimaryButton(sendBtn);

        add(top, BorderLayout.NORTH);
        add(chatScroll, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        sendBtn.addActionListener(e -> doSend());
        clearBtn.addActionListener(e -> doClear());
        lookupBtn.addActionListener(e -> doLookup());
        inputField.addActionListener(e -> doSend());

        appendChat("System", I18n.t("admin.ai.welcome"));
    }

    private void doSend() {
        String text = inputField.getText().trim();
        if (text.isEmpty()) return;
        inputField.setText("");
        appendChat("You", text);

        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", text);
        history.add(userMsg);

        String endpoint = endpointField.getText().trim();
        String apiKey   = apiKeyField.getText().trim();
        String model    = String.valueOf(modelBox.getSelectedItem());

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> sys = new LinkedHashMap<>();
        sys.put("role", "system");
        sys.put("content", buildSystemPrompt());
        messages.add(sys);
        messages.addAll(history);

        setInputEnabled(false);
        statusLabel.setText(I18n.t("admin.ai.thinking"));

        new SwingWorker<String, Void>() {
            @Override protected String doInBackground() throws Exception {
                return callApi(endpoint, apiKey, model, messages);
            }
            @Override protected void done() {
                try {
                    String reply = get();
                    Map<String, Object> assistantMsg = new LinkedHashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", reply);
                    history.add(assistantMsg);
                    appendChat("AI", reply);
                } catch (Exception ex) {
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    appendChat("Error", I18n.t("admin.ai.error", msg));
                }
                statusLabel.setText(" ");
                setInputEnabled(true);
                inputField.requestFocus();
            }
        }.execute();
    }

    private void doLookup() {
        String query = JOptionPane.showInputDialog(this,
            I18n.t("admin.ai.lookup.prompt"), I18n.t("admin.ai.lookup"), JOptionPane.PLAIN_MESSAGE);
        if (query == null || query.trim().isEmpty()) return;
        String q = query.trim().toLowerCase();
        StringBuilder result = new StringBuilder();
        if (data != null) {
            for (ebu6304.storage.AuthStore.User u : data.listUsers()) {
                if (u.account().toLowerCase().contains(q) || u.name().toLowerCase().contains(q)) {
                    result.append("[").append(I18n.t("admin.ai.lookup.account")).append("] admin_system.xml\n");
                    result.append("  account  = ").append(u.account()).append("\n");
                    result.append("  name     = ").append(u.name()).append("\n");
                    result.append("  role     = ").append(u.role()).append("\n");
                    result.append("  enabled  = ").append(u.enabled()).append("\n\n");
                }
            }
            for (Applicant a : data.listApplicants()) {
                if (a.id().toLowerCase().contains(q) || a.name().toLowerCase().contains(q)
                        || a.email().toLowerCase().contains(q)) {
                    result.append("[").append(I18n.t("admin.ai.lookup.applicant")).append("] ta_info.csv\n");
                    result.append("  id          = ").append(a.id()).append("\n");
                    result.append("  name        = ").append(a.name()).append("\n");
                    result.append("  email       = ").append(a.email()).append("\n");
                    result.append("  skills      = ").append(a.skills()).append("\n");
                    result.append("  cvPath      = ").append(a.cvPath()).append("\n");
                    result.append("  description = ").append(a.description()).append("\n");
                    List<Application> apps = data.listApplicationsForApplicant(a.id());
                    result.append("  applications = ").append(apps.size()).append("\n");
                    for (Application ap : apps) {
                        result.append("    - job=").append(ap.jobId())
                              .append(" status=").append(ap.status()).append("\n");
                    }
                    result.append("\n");
                }
            }
        }
        String display = result.length() == 0
            ? I18n.t("admin.ai.lookup.notfound") + ": " + query
            : result.toString();
        JTextArea area = new JTextArea(display, 18, 60);
        area.setEditable(false);
        area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        JOptionPane.showMessageDialog(this, new JScrollPane(area),
            I18n.t("admin.ai.lookup") + ": " + query, JOptionPane.INFORMATION_MESSAGE);
    }

    private void doClear() {
        history.clear();
        chatMessages.clear();
        chatPane.setText("");
        appendChat("System", I18n.t("admin.ai.welcome"));
    }

    private void setInputEnabled(boolean enabled) {
        inputField.setEnabled(enabled);
        sendBtn.setEnabled(enabled);
    }

    private String buildSystemPrompt() {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an AI assistant for the BUPT International School TA Recruitment System administrator.\n");
        sb.append("You have access to the full current system data below. Use it to answer questions accurately.\n\n");

        if (data != null) {
            String dataDir = data.dataDir().toAbsolutePath().toString();
            try {
                List<Applicant> applicants = data.listApplicants();
                sb.append("=== TA APPLICANTS (").append(applicants.size())
                  .append(") [file: ").append(dataDir).append("/ta_info.csv] ===\n");
                for (Applicant a : applicants) {
                    sb.append("- ID:").append(a.id()).append(" | Name:").append(a.name())
                      .append(" | Email:").append(a.email()).append(" | Skills:").append(a.skills());
                    if (!a.description().isEmpty()) sb.append(" | Desc:").append(a.description());
                    if (!a.cvPath().isEmpty()) {
                        try {
                            java.nio.file.Path cvFile = java.nio.file.Paths.get(a.cvPath().trim());
                            if (!cvFile.isAbsolute() || !java.nio.file.Files.exists(cvFile)) {
                                String fname = cvFile.getFileName() != null
                                    ? cvFile.getFileName().toString()
                                    : java.nio.file.Paths.get(a.cvPath().trim().replace("\\", "/")).getFileName().toString();
                                java.nio.file.Path local = data.dataDir().resolve("cv").resolve(fname);
                                if (java.nio.file.Files.exists(local)) cvFile = local;
                            }
                            String cvText = ResumeTextExtractor.extract(cvFile);
                            if (cvText != null && !cvText.isBlank()) {
                                String excerpt = cvText.length() > 600 ? cvText.substring(0, 600) + "..." : cvText;
                                sb.append("\n  [CV] ").append(excerpt.replace("\n", " | "));
                            }
                        } catch (Exception ignored2) {}
                    }
                    sb.append("\n");
                }
            } catch (Exception ignored) {}

            try {
                List<Job> jobs = data.listJobs();
                sb.append("\n=== JOBS (").append(jobs.size())
                  .append(") [file: ").append(dataDir).append("/mo_jobs.json] ===\n");
                for (Job j : jobs) {
                    sb.append("- ID:").append(j.id()).append(" | Title:").append(j.title())
                      .append(" | Skills:").append(j.requiredSkills())
                      .append(" | Hours:").append(j.hoursPerWeek())
                      .append(" | Status:").append(j.status())
                      .append(" | By:").append(j.postedBy()).append("\n");
                }
            } catch (Exception ignored) {}

            try {
                List<Job> allJobs = data.listJobs();
                List<Application> allApps = new ArrayList<>();
                for (Job j : allJobs) allApps.addAll(data.listApplicationsForJob(j.id()));
                sb.append("\n=== APPLICATIONS (").append(allApps.size())
                  .append(") [embedded in mo_jobs.json] ===\n");
                for (Application ap : allApps) {
                    sb.append("- applicant=").append(ap.applicantId())
                      .append(" job=").append(ap.jobId())
                      .append(" status=").append(ap.status()).append("\n");
                }
            } catch (Exception ignored) {}

            try {
                List<ebu6304.storage.AuthStore.User> users = data.listUsers();
                sb.append("\n=== ACCOUNTS (").append(users.size())
                  .append(" total) [file: ").append(dataDir).append("/admin_system.xml] ===\n");
                java.util.Map<String, List<ebu6304.storage.AuthStore.User>> byRole = new java.util.LinkedHashMap<>();
                for (ebu6304.storage.AuthStore.User u : users)
                    byRole.computeIfAbsent(u.role(), k -> new ArrayList<>()).add(u);
                for (java.util.Map.Entry<String, List<ebu6304.storage.AuthStore.User>> e : byRole.entrySet()) {
                    sb.append(e.getKey()).append(" (").append(e.getValue().size()).append("):\n");
                    for (ebu6304.storage.AuthStore.User u : e.getValue())
                        sb.append("  - ").append(u.account()).append(" [").append(u.name())
                          .append("] enabled=").append(u.enabled()).append("\n");
                }
            } catch (Exception ignored) {}
        }

        sb.append("\n=== DATA NOTES ===\n");
        sb.append("Active files: ta_info.csv, mo_jobs.json (applications embedded), admin_system.xml, data/cv/\n");
        sb.append("UNUSED/LEGACY: applicants.tsv, applications.tsv, jobs.tsv\n");
        sb.append("\nRespond in the same language the user uses. Never modify data automatically.");
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private String callApi(String endpoint, String apiKey, String model,
                           List<Map<String, Object>> messages) throws Exception {
        String urlStr = endpoint.endsWith("/")
            ? endpoint + "chat/completions"
            : endpoint + "/chat/completions";
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", 0.7);
        body.put("max_tokens", 2048);
        byte[] payload = MiniJson.stringify(body).getBytes(StandardCharsets.UTF_8);
        URL url = new java.net.URI(urlStr).toURL();
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(90000);
        conn.setDoOutput(true);
        try (OutputStream os = conn.getOutputStream()) { os.write(payload); }
        int code = conn.getResponseCode();
        InputStream is = code < 300 ? conn.getInputStream() : conn.getErrorStream();
        byte[] buf = is.readAllBytes();
        String resp = new String(buf, StandardCharsets.UTF_8);
        Object root = MiniJson.parse(resp);
        if (root instanceof Map) {
            Object choices = ((Map<String, Object>) root).get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object msg = ((List<?>) choices).get(0);
                if (msg instanceof Map) {
                    Object m2 = ((Map<String, Object>) msg).get("message");
                    if (m2 instanceof Map) {
                        Object c = ((Map<String, Object>) m2).get("content");
                        if (c != null) return c.toString();
                    }
                }
            }
        }
        throw new IOException("Unexpected API response: " + resp.substring(0, Math.min(200, resp.length())));
    }

    private void appendChat(String who, String text) {
        chatMessages.add(new String[]{who, text});
        rebuildHtml();
    }

    private void rebuildHtml() {
        StringBuilder html = new StringBuilder();
        html.append("<html><body style='font-family:Dialog,sans-serif;font-size:13px;margin:6px;'>");
        for (String[] msg : chatMessages) {
            String who = msg[0]; String txt = msg[1];
            String color = "You".equals(who) ? "#1565C0" : "AI".equals(who) ? "#2E7D32"
                         : "Error".equals(who) ? "#C62828" : "#888888";
            html.append("<div style='margin-bottom:8px;'>");
            html.append("<span style='color:").append(color).append(";font-weight:bold;'>")
                .append(escHtml(who)).append(": </span>");
            html.append(mdToHtml(txt));
            html.append("</div>");
        }
        html.append("</body></html>");
        chatPane.setText(html.toString());
        chatPane.setCaretPosition(chatPane.getDocument().getLength());
    }

    private static String escHtml(String s) {
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    private static String mdToHtml(String md) {
        if (md == null) return "";
        String[] lines = md.split("\n", -1);
        StringBuilder out = new StringBuilder();
        boolean inUl = false; boolean inOl = false; boolean inTable = false;
        for (String raw : lines) {
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
                if (!inTable) { out.append("<table border='0' cellpadding='5' cellspacing='0' style='border-collapse:collapse;margin:6px 0;'>"); inTable = true; }
                String[] cells = raw.split("\\|", -1);
                out.append("<tr>");
                for (int ci = 1; ci < cells.length - 1; ci++)
                    out.append("<td style='border:1px solid #ccc;padding:4px 8px;'>").append(inlineHtml(cells[ci].trim())).append("</td>");
                out.append("</tr>"); continue;
            }
            if (isHr) { out.append("<hr style='border:none;border-top:1px solid #ddd;margin:8px 0'>"); continue; }
            if (raw.startsWith("##### ")) { out.append("<h5 style='margin:4px 0'>").append(inlineHtml(raw.substring(6))).append("</h5>"); continue; }
            if (raw.startsWith("#### "))  { out.append("<h4 style='margin:4px 0'>").append(inlineHtml(raw.substring(5))).append("</h4>"); continue; }
            if (raw.startsWith("### "))   { out.append("<h4 style='margin:4px 0'>").append(inlineHtml(raw.substring(4))).append("</h4>"); continue; }
            if (raw.startsWith("## "))    { out.append("<h3 style='margin:4px 0'>").append(inlineHtml(raw.substring(3))).append("</h3>"); continue; }
            if (raw.startsWith("# "))     { out.append("<h3 style='margin:4px 0'>").append(inlineHtml(raw.substring(2))).append("</h3>"); continue; }
            if (isBullet) {
                if (!inUl) { out.append("<ul style='margin:2px 0;padding-left:20px'>"); inUl = true; }
                out.append("<li>").append(inlineHtml(raw.replaceFirst("^\\s*[-*] ", ""))).append("</li>"); continue;
            }
            if (isNum) {
                if (!inOl) { out.append("<ol style='margin:2px 0;padding-left:20px'>"); inOl = true; }
                out.append("<li>").append(inlineHtml(raw.replaceFirst("^\\s*\\d+[.)]\\ ", ""))).append("</li>"); continue;
            }
            if (raw.trim().isEmpty()) { out.append("<br>"); continue; }
            out.append("<p style='margin:2px 0'>").append(inlineHtml(raw)).append("</p>");
        }
        if (inUl) out.append("</ul>");
        if (inOl) out.append("</ol>");
        if (inTable) out.append("</table>");
        return out.toString();
    }

    private static String inlineHtml(String text) {
        text = escHtml(text);
        text = text.replaceAll("\\*\\*(.+?)\\*\\*", "<b>$1</b>");
        text = text.replaceAll("\\*(.+?)\\*",       "<i>$1</i>");
        text = text.replaceAll("`(.+?)`",            "<code style='background:#f0f0f0;padding:0 3px'>$1</code>");
        return text;
    }
}
