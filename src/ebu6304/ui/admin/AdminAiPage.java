package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingWorker;

import ebu6304.storage.DataService;
import ebu6304.storage.MiniJson;
import ebu6304.ui.I18n;

public final class AdminAiPage extends JPanel {

    private static final String DEFAULT_ENDPOINT = "https://token-plan-cn.xiaomimimo.com/v1";
    private static final String DEFAULT_API_KEY = "tp-cod47ehotbktykmfmeb02oxkxt777874ern2bfslnjazju1m";
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
    private final JButton lookupBtn = new JButton("🔍 查用户");
    private final JComboBox<String> modelBox = new JComboBox<>(MODELS);
    private final JTextField apiKeyField = new JTextField(DEFAULT_API_KEY, 28);
    private final JTextField endpointField = new JTextField(DEFAULT_ENDPOINT, 32);
    private final JLabel statusLabel = new JLabel(" ");

    public AdminAiPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        setBorder(BorderFactory.createTitledBorder(I18n.t("nav.admin.ai")));

        JPanel top = new JPanel(new BorderLayout(10, 5));
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        toolbar.add(new JLabel(I18n.t("admin.ai.endpoint")));
        toolbar.add(endpointField);
        toolbar.add(new JLabel(I18n.t("admin.ai.apikey")));
        toolbar.add(apiKeyField);
        toolbar.add(new JLabel(I18n.t("admin.ai.model")));
        toolbar.add(modelBox);
        toolbar.add(clearBtn);
        toolbar.add(lookupBtn);
        top.add(toolbar, BorderLayout.CENTER);

        chatPane.setEditable(false);
        chatPane.setContentType("text/html");
        JScrollPane chatScroll = new JScrollPane(chatPane);
        chatScroll.setPreferredSize(new Dimension(600, 400));

        statusLabel.setForeground(Color.GRAY);
        JPanel bottom = new JPanel(new BorderLayout(6, 4));
        bottom.add(statusLabel, BorderLayout.NORTH);
        JPanel inputRow = new JPanel(new BorderLayout(6, 0));
        sendBtn.setPreferredSize(new Dimension(80, sendBtn.getPreferredSize().height));
        inputRow.add(inputField, BorderLayout.CENTER);
        inputRow.add(sendBtn, BorderLayout.EAST);
        bottom.add(inputRow, BorderLayout.CENTER);

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
        setInputEnabled(false);
        statusLabel.setText(I18n.t("admin.ai.thinking"));

        appendChat("You", text);

        Map<String, Object> userMsg = new LinkedHashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", text);
        history.add(userMsg);

        final String model = (String) modelBox.getSelectedItem();
        final String endpoint = endpointField.getText().trim();
        final String apiKey = apiKeyField.getText().trim();

        List<Map<String, Object>> messages = new ArrayList<>();
        Map<String, Object> sys = new LinkedHashMap<>();
        sys.put("role", "system");
        sys.put("content", buildSystemPrompt());
        messages.add(sys);
        messages.addAll(history);

        SwingWorker<String, Void> worker = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return callApi(endpoint, apiKey, model, messages);
            }

            @Override
            protected void done() {
                try {
                    String reply = get();
                    Map<String, Object> assistantMsg = new LinkedHashMap<>();
                    assistantMsg.put("role", "assistant");
                    assistantMsg.put("content", reply);
                    history.add(assistantMsg);
                    appendChat("AI", reply);
                } catch (Exception ex) {
                    if (!history.isEmpty()) history.remove(history.size() - 1);
                    String msg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
                    appendChat("Error", I18n.t("admin.ai.error", msg));
                }
                statusLabel.setText(" ");
                setInputEnabled(true);
                inputField.requestFocus();
            }
        };
        worker.execute();
    }

    private void doLookup() {
        String query = javax.swing.JOptionPane.showInputDialog(this, "输入账号或姓名查询：", "查用户/申请人", javax.swing.JOptionPane.PLAIN_MESSAGE);
        if (query == null || query.trim().isEmpty()) return;
        String q = query.trim().toLowerCase();
        StringBuilder result = new StringBuilder();
        if (data != null) {
            for (ebu6304.storage.AuthStore.User u : data.listUsers()) {
                if (u.account().toLowerCase().contains(q) || u.name().toLowerCase().contains(q)) {
                    result.append("[账户记录] 文件: admin_system.xml <users>\n");
                    result.append("  account  = ").append(u.account()).append("\n");
                    result.append("  name     = ").append(u.name()).append("\n");
                    result.append("  role     = ").append(u.role()).append("\n");
                    result.append("  enabled  = ").append(u.enabled()).append("\n\n");
                }
            }
            for (ebu6304.model.Applicant a : data.listApplicants()) {
                if (a.id().toLowerCase().contains(q) || a.name().toLowerCase().contains(q) || a.email().toLowerCase().contains(q)) {
                    result.append("[申请人档案] 文件: ta_info.csv\n");
                    result.append("  id          = ").append(a.id()).append("\n");
                    result.append("  name        = ").append(a.name()).append("\n");
                    result.append("  email       = ").append(a.email()).append("\n");
                    result.append("  skills      = ").append(a.skills()).append("\n");
                    result.append("  cvPath      = ").append(a.cvPath()).append("\n");
                    result.append("  description = ").append(a.description()).append("\n");
                    java.io.File cvFile = resolveFile(a.cvPath(), a.id());
                    result.append("  CV文件(实际路径) = ").append(cvFile != null ? cvFile.getAbsolutePath() : "未找到").append("\n");
                    List<ebu6304.model.Application> apps = data.listApplicationsForApplicant(a.id());
                    result.append("  申请记录数  = ").append(apps.size()).append("\n");
                    for (ebu6304.model.Application ap : apps) {
                        result.append("    - jobId=").append(ap.jobId()).append(" status=").append(ap.status()).append("\n");
                    }
                    result.append("\n");
                }
            }
        }
        String display = result.length() == 0 ? "未找到匹配记录：" + query : result.toString();
        javax.swing.JTextArea area = new javax.swing.JTextArea(display, 18, 60);
        area.setEditable(false);
        area.setFont(new java.awt.Font(java.awt.Font.MONOSPACED, java.awt.Font.PLAIN, 13));
        javax.swing.JOptionPane.showMessageDialog(this, new javax.swing.JScrollPane(area), "查询结果：" + query, javax.swing.JOptionPane.INFORMATION_MESSAGE);
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
                List<ebu6304.model.Applicant> applicants = data.listApplicants();
                sb.append("=== TA APPLICANTS (").append(applicants.size()).append(") [file: ").append(dataDir).append("/ta_info.csv] ===\n");
                for (ebu6304.model.Applicant a : applicants) {
                    sb.append("- ID: ").append(a.id())
                      .append(" | Name: ").append(a.name())
                      .append(" | Email: ").append(a.email())
                      .append(" | Skills: ").append(a.skills());
                    if (!a.description().isEmpty()) {
                        sb.append(" | Description: ").append(a.description());
                    }
                    String cvText = extractCvText(a.cvPath(), a.id());
                    if (!cvText.isEmpty()) {
                        sb.append("\n  [CV Content]: ").append(cvText);
                    } else if (!a.cvPath().isEmpty()) {
                        sb.append(" | CV: ").append(a.cvPath());
                    }
                    sb.append("\n");
                }
            } catch (Exception ignored) {}

            try {
                List<ebu6304.model.Job> jobs = data.listJobs();
                sb.append("\n=== JOB POSTINGS (").append(jobs.size()).append(") [file: ").append(dataDir).append("/mo_jobs.json] ===\n");
                for (ebu6304.model.Job j : jobs) {
                    sb.append("- ID: ").append(j.id())
                      .append(" | Title: ").append(j.title())
                      .append(" | Skills: ").append(j.requiredSkills())
                      .append(" | Hours/week: ").append(j.hoursPerWeek())
                      .append(" | Status: ").append(j.status())
                      .append(" | Posted by: ").append(j.postedBy());
                    if (!j.description().isEmpty()) {
                        sb.append(" | Desc: ").append(j.description());
                    }
                    sb.append("\n");
                }
            } catch (Exception ignored) {}

            try {
                List<ebu6304.model.Job> allJobs = data.listJobs();
                List<ebu6304.model.Application> allApps = new java.util.ArrayList<>();
                for (ebu6304.model.Job j : allJobs) {
                    allApps.addAll(data.listApplicationsForJob(j.id()));
                }
                sb.append("\n=== APPLICATIONS (").append(allApps.size()).append(") [embedded in: ").append(dataDir).append("/mo_jobs.json, each job.applications[]] ===\n");
                for (ebu6304.model.Application ap : allApps) {
                    sb.append("- AppID: ").append(ap.id())
                      .append(" | Applicant: ").append(ap.applicantId())
                      .append(" | Job: ").append(ap.jobId())
                      .append(" | Status: ").append(ap.status())
                      .append("\n");
                }
            } catch (Exception ignored) {}

            try {
                List<ebu6304.storage.AuthStore.User> users = data.listUsers();
                sb.append("\n=== ACCOUNTS (").append(users.size()).append(" total) [file: ").append(dataDir).append("/admin_system.xml, <users> element] ===\n");
                java.util.Map<String, List<ebu6304.storage.AuthStore.User>> byRole = new java.util.LinkedHashMap<>();
                for (ebu6304.storage.AuthStore.User u : users) {
                    byRole.computeIfAbsent(u.role(), k -> new java.util.ArrayList<>()).add(u);
                }
                for (java.util.Map.Entry<String, List<ebu6304.storage.AuthStore.User>> e : byRole.entrySet()) {
                    sb.append(e.getKey()).append(" (").append(e.getValue().size()).append("):\n");
                    for (ebu6304.storage.AuthStore.User u : e.getValue()) {
                        sb.append("  - account=").append(u.account())
                          .append(", name=").append(u.name())
                          .append(", enabled=").append(u.enabled()).append("\n");
                    }
                }
            } catch (Exception ignored) {}
        }

        sb.append("\n=== DATA STORAGE NOTES ===\n");
        sb.append("Active files: ta_info.csv (applicants), mo_jobs.json (jobs+applications embedded), admin_system.xml (accounts), data/cv/ (PDF resumes), temp_operation.txt (logs).\n");
        sb.append("LEGACY/UNUSED files (NOT loaded): applicants.tsv, applications.tsv, jobs.tsv — ignore these.\n");
        sb.append("Applications are embedded inside mo_jobs.json under each job's 'applications' array.\n");
        sb.append("CV path fallback: if stored path missing, tries data/cv/{applicantId}.pdf automatically.\n");
        sb.append("\nAnswer concisely and helpfully. Respond in the same language the user uses (Chinese or English).");
        return sb.toString();
    }

    private java.io.File resolveFile(String cvPath, String applicantId) {
        if (cvPath != null && !cvPath.isEmpty()) {
            java.io.File f = new java.io.File(cvPath);
            if (f.exists() && f.isFile()) return f;
        }
        if (data != null) {
            java.nio.file.Path cvDir = data.dataDir().resolve("cv");
            if (applicantId != null && !applicantId.isEmpty()) {
                java.io.File f = cvDir.resolve(applicantId + ".pdf").toFile();
                if (f.exists()) return f;
                f = cvDir.resolve(applicantId + ".txt").toFile();
                if (f.exists()) return f;
            }
            if (cvPath != null && !cvPath.isEmpty()) {
                String basename = new java.io.File(cvPath).getName();
                java.io.File f = cvDir.resolve(basename).toFile();
                if (f.exists()) return f;
            }
        }
        return null;
    }

    private String extractCvText(String cvPath, String applicantId) {
        java.io.File f = resolveFile(cvPath, applicantId);
        if (f == null) return "";
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(f.toPath());
            String name = f.getName().toLowerCase();
            if (name.endsWith(".txt")) {
                String text = new String(bytes, StandardCharsets.UTF_8).trim();
                return text.length() > 3000 ? text.substring(0, 3000) + "..." : text;
            }
            if (name.endsWith(".pdf")) {
                String text = extractPdfText(bytes);
                return text.length() > 3000 ? text.substring(0, 3000) + "..." : text;
            }
        } catch (Exception ignored) {}
        return "";
    }

    private String extractPdfText(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        String raw = new String(bytes, StandardCharsets.ISO_8859_1);
        int i = 0;
        while (i < raw.length()) {
            int dictEnd = raw.indexOf("stream", i);
            if (dictEnd < 0) break;
            String dictPart = raw.substring(Math.max(i, dictEnd - 300), dictEnd);
            boolean isFlate = dictPart.contains("FlateDecode") || dictPart.contains("Fl\n") || dictPart.contains("/Fl ");
            int streamStart = dictEnd + 6;
            if (streamStart < raw.length() && raw.charAt(streamStart) == '\r') streamStart++;
            if (streamStart < raw.length() && raw.charAt(streamStart) == '\n') streamStart++;
            int streamEnd = raw.indexOf("endstream", streamStart);
            if (streamEnd < 0) break;
            while (streamEnd > streamStart && (raw.charAt(streamEnd - 1) == '\r' || raw.charAt(streamEnd - 1) == '\n')) {
                streamEnd--;
            }
            try {
                byte[] streamBytes = raw.substring(streamStart, streamEnd).getBytes(StandardCharsets.ISO_8859_1);
                String content;
                if (isFlate) {
                    content = inflate(streamBytes);
                } else {
                    content = new String(streamBytes, StandardCharsets.ISO_8859_1);
                }
                if (content != null) {
                    result.append(extractTextOps(content));
                }
            } catch (Exception ignored) {}
            i = streamEnd + 9;
        }
        return result.toString().replaceAll("\\s+", " ").trim();
    }

    private String inflate(byte[] compressed) {
        try {
            java.util.zip.Inflater inf = new java.util.zip.Inflater();
            inf.setInput(compressed);
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream(compressed.length * 4);
            byte[] buf = new byte[4096];
            while (!inf.finished() && !inf.needsInput()) {
                int n = inf.inflate(buf);
                if (n > 0) out.write(buf, 0, n);
            }
            inf.end();
            return out.toString("ISO-8859-1");
        } catch (Exception e) {
            return null;
        }
    }

    private String extractTextOps(String content) {
        StringBuilder text = new StringBuilder();
        java.util.regex.Matcher m = java.util.regex.Pattern.compile(
            "\\(([^)]{1,300})\\)\\s*T[jJ']|\\[([^\\]]{1,600})\\]\\s*TJ"
        ).matcher(content);
        while (m.find()) {
            if (m.group(1) != null) {
                String s = cleanPdfString(m.group(1));
                if (!s.isEmpty()) text.append(s).append(" ");
            } else if (m.group(2) != null) {
                java.util.regex.Matcher sub = java.util.regex.Pattern.compile("\\(([^)]{1,300})\\)").matcher(m.group(2));
                while (sub.find()) {
                    String s = cleanPdfString(sub.group(1));
                    if (!s.isEmpty()) text.append(s);
                }
                text.append(" ");
            }
        }
        return text.toString();
    }

    private String cleanPdfString(String s) {
        return s.replaceAll("[\\x00-\\x08\\x0b\\x0c\\x0e-\\x1f]", "")
                .replaceAll("\\\\([nrtbf\\\\()])", " ")
                .trim();
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

        try (OutputStream os = conn.getOutputStream()) {
            os.write(payload);
        }

        int code = conn.getResponseCode();
        InputStream is = code >= 400 ? conn.getErrorStream() : conn.getInputStream();
        StringBuilder resp = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) resp.append(line).append("\n");
        }

        if (code >= 400) {
            throw new IOException("HTTP " + code + ": " + resp.toString().trim());
        }

        Object parsed = MiniJson.parse(resp.toString());
        if (parsed instanceof Map) {
            Object choices = ((Map<String, Object>) parsed).get("choices");
            if (choices instanceof List && !((List<?>) choices).isEmpty()) {
                Object first = ((List<Object>) choices).get(0);
                if (first instanceof Map) {
                    Object msg = ((Map<String, Object>) first).get("message");
                    if (msg instanceof Map) {
                        Object content = ((Map<String, Object>) msg).get("content");
                        if (content != null) return content.toString();
                    }
                }
            }
        }
        throw new IOException("Unexpected API response: " + resp.toString().trim());
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
            html.append("<span style='color:").append(color).append(";font-weight:bold;'>").append(escHtml(who)).append(": </span>");
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
        boolean inUl = false; boolean inOl = false;
        for (String raw : lines) {
            String line = raw;
            boolean isBullet = line.matches("^\\s*[-*] .+");
            boolean isNumList = line.matches("^\\s*\\d+[.)]\\ .+");
            if (!isBullet && inUl)  { out.append("</ul>");  inUl  = false; }
            if (!isNumList && inOl) { out.append("</ol>");  inOl  = false; }
            if (line.startsWith("### ")) { out.append("<h4 style='margin:4px 0'>").append(inlineHtml(line.substring(4))).append("</h4>"); continue; }
            if (line.startsWith("## "))  { out.append("<h3 style='margin:4px 0'>").append(inlineHtml(line.substring(3))).append("</h3>"); continue; }
            if (line.startsWith("# "))   { out.append("<h3 style='margin:4px 0'>").append(inlineHtml(line.substring(2))).append("</h3>"); continue; }
            if (isBullet) {
                if (!inUl) { out.append("<ul style='margin:2px 0;padding-left:18px'>"); inUl = true; }
                String content = line.replaceFirst("^\\s*[-*] ", "");
                out.append("<li>").append(inlineHtml(content)).append("</li>");
                continue;
            }
            if (isNumList) {
                if (!inOl) { out.append("<ol style='margin:2px 0;padding-left:18px'>"); inOl = true; }
                String content = line.replaceFirst("^\\s*\\d+[.)]\\ ", "");
                out.append("<li>").append(inlineHtml(content)).append("</li>");
                continue;
            }
            if (line.trim().isEmpty()) { out.append("<br>"); continue; }
            out.append("<p style='margin:2px 0'>").append(inlineHtml(line)).append("</p>");
        }
        if (inUl) out.append("</ul>");
        if (inOl) out.append("</ol>");
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
