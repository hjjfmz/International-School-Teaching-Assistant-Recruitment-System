package ebu6304.ui;

import java.awt.BorderLayout;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.Timer;

import ebu6304.ai.AiModule;
import ebu6304.storage.DataService;
import ebu6304.ui.ta.TaApplicationStatusPage;
import ebu6304.ui.ta.TaHomePage;
import ebu6304.ui.ta.TaJobsPage;
import ebu6304.ui.ta.TaMyApplicationsPage;
import ebu6304.ui.ta.TaProfilePage;
import ebu6304.ui.ta.TaResumePage;
import ebu6304.ui.mo.MoApplicantsPage;
import ebu6304.ui.mo.MoHomePage;
import ebu6304.ui.mo.MoMyPostsPage;
import ebu6304.ui.mo.MoPostJobPage;
import ebu6304.ui.mo.MoResultsPage;
import ebu6304.ui.admin.AdminConfigPage;
import ebu6304.ui.admin.AdminExportPage;
import ebu6304.ui.admin.AdminHomePage;
import ebu6304.ui.admin.AdminJobDataPage;
import ebu6304.ui.admin.AdminLogPage;
import ebu6304.ui.admin.AdminUserManagementPage;
import ebu6304.ui.admin.AdminAiPage;
import ebu6304.ui.admin.AdminWorkloadPage;

public final class WorkbenchPanel extends JPanel {
    private final AppLayout layout;
    private Timer notifTimer;
    private int lastReadLineCount;
    private int lastSeenLineCount;
    private int filteredUnreadCount;
    private final Role notifRole;
    private final String notifAccount;
    private final DataService notifData;

    public WorkbenchPanel(DataService data, Role role, String account, Runnable logout, Runnable onLanguageChange) {
        super(new BorderLayout());
        this.notifRole = role;
        this.notifAccount = account;
        this.notifData = data;

        String[] nav;
        if (role == Role.TA) {
            nav = new String[] {
                I18n.t("nav.ta.home"),
                I18n.t("nav.ta.profile"),
                I18n.t("nav.ta.resume"),
                I18n.t("nav.ta.jobs"),
                I18n.t("nav.ta.myapps"),
                I18n.t("nav.ta.status")
            };
        } else if (role == Role.MO) {
            nav = new String[] {
                I18n.t("nav.mo.home"),
                I18n.t("nav.mo.post"),
                I18n.t("nav.mo.applicants"),
                I18n.t("nav.mo.results"),
                I18n.t("nav.mo.myposts")
            };
        } else {
            nav = new String[] {
                I18n.t("nav.admin.home"),
                I18n.t("nav.admin.users"),
                I18n.t("nav.admin.workload"),
                I18n.t("nav.admin.jobdata"),
                I18n.t("nav.admin.config"),
                I18n.t("nav.admin.export"),
                I18n.t("nav.admin.logs"),
                I18n.t("nav.admin.ai")
            };
        }

        final AppLayout[] holder = new AppLayout[1];
        holder[0] = new AppLayout(role, nav, () -> {
            if (logout != null) logout.run();
        }, key -> {
            if (key == null) return;
            holder[0].showContent(key);
        }, onLanguageChange);
        layout = holder[0];

        layout.setUser(role, account);
        startOperationLogNotifications();
        AiModule aiModule = new AiModule(data);

        if (role == Role.TA) {
            TaHomePage home = new TaHomePage(data, account, k -> layout.showContent(k));
            TaProfilePage profile = new TaProfilePage(data, account, aiModule.aiIndexController());
            TaResumePage resume = new TaResumePage(data, account, aiModule.aiIndexController());
            TaJobsPage jobs = new TaJobsPage(data, account, aiModule.jobRecommendationController(),
                    () -> { layout.showContent(I18n.t("nav.ta.home")); layout.setNavSelectedIndex(0); });
            TaMyApplicationsPage myApps = new TaMyApplicationsPage(data, account);
            TaApplicationStatusPage status = new TaApplicationStatusPage(data, account, () -> { layout.showContent(I18n.t("nav.ta.home")); layout.setNavSelectedIndex(0); });

            layout.addContent(I18n.t("nav.ta.home"), home);
            layout.addContent(I18n.t("nav.ta.profile"), profile);
            layout.addContent(I18n.t("nav.ta.resume"), resume);
            layout.addContent(I18n.t("nav.ta.jobs"), jobs);
            layout.addContent(I18n.t("nav.ta.myapps"), myApps);
            layout.addContent(I18n.t("nav.ta.status"), status);

            layout.showContent(I18n.t("nav.ta.home"));
            layout.setNavSelectedIndex(0);
        } else if (role == Role.MO) {
            MoHomePage home = new MoHomePage(data, account, k -> layout.showContent(k));
            MoPostJobPage post = new MoPostJobPage(data, account, aiModule.jdAssistantController(), aiModule.aiIndexController());
            MoApplicantsPage applicants = new MoApplicantsPage(data, account, aiModule.applicantMatchController());
            MoResultsPage results = new MoResultsPage(data, account);
            MoMyPostsPage myPosts = new MoMyPostsPage(data, account);

            layout.addContent(I18n.t("nav.mo.home"), home);
            layout.addContent(I18n.t("nav.mo.post"), post);
            layout.addContent(I18n.t("nav.mo.applicants"), applicants);
            layout.addContent(I18n.t("nav.mo.results"), results);
            layout.addContent(I18n.t("nav.mo.myposts"), myPosts);

            layout.showContent(I18n.t("nav.mo.home"));
            layout.setNavSelectedIndex(0);
        } else {
            AdminHomePage home = new AdminHomePage(data, k -> layout.showContent(k));
            AdminUserManagementPage users = new AdminUserManagementPage(data, account);
            AdminWorkloadPage workload = new AdminWorkloadPage(data, account);
            AdminJobDataPage jobs = new AdminJobDataPage(data, account);
            AdminConfigPage config = new AdminConfigPage(data, account);
            AdminExportPage export = new AdminExportPage(data, account);
            AdminLogPage logs = new AdminLogPage(data);

            layout.addContent(I18n.t("nav.admin.home"), home);
            layout.addContent(I18n.t("nav.admin.users"), users);
            layout.addContent(I18n.t("nav.admin.workload"), workload);
            layout.addContent(I18n.t("nav.admin.jobdata"), jobs);
            layout.addContent(I18n.t("nav.admin.config"), config);
            layout.addContent(I18n.t("nav.admin.export"), export);
            AdminAiPage ai = new AdminAiPage(data);
            layout.addContent(I18n.t("nav.admin.logs"), logs);
            layout.addContent(I18n.t("nav.admin.ai"), ai);

            layout.showContent(I18n.t("nav.admin.home"));
            layout.setNavSelectedIndex(0);
        }

        add(layout, BorderLayout.CENTER);
    }

    private void startOperationLogNotifications() {
        if (notifData == null) return;
        final Path log = notifData.tempOperationFile();
        lastReadLineCount = safeLineCount(log);
        lastSeenLineCount = lastReadLineCount;
        filteredUnreadCount = 0;
        layout.setUnreadNotifications(0);

        layout.setNotificationsTextSupplier(() -> buildNotificationsText(log, 200, notifRole, notifAccount, notifData));

        layout.setOnNotificationsOpened(() -> {
            filteredUnreadCount = 0;
            lastReadLineCount = lastSeenLineCount;
            layout.setUnreadNotifications(0);
        });

        notifTimer = new Timer(2000, e -> {
            int totalLines = safeLineCount(log);
            if (totalLines < lastSeenLineCount) {
                lastSeenLineCount = totalLines;
                lastReadLineCount = totalLines;
                filteredUnreadCount = 0;
            } else if (totalLines > lastSeenLineCount) {
                try {
                    List<String> allLines = Files.readAllLines(log, StandardCharsets.UTF_8);
                    for (int i = lastSeenLineCount; i < Math.min(totalLines, allLines.size()); i++) {
                        if (isLineRelevant(allLines.get(i), notifRole, notifAccount, notifData)) {
                            filteredUnreadCount++;
                        }
                    }
                } catch (Exception ignored) {
                }
                lastSeenLineCount = totalLines;
            }
            layout.setUnreadNotifications(filteredUnreadCount);
        });
        notifTimer.setRepeats(true);
        notifTimer.start();

        addHierarchyListener(e -> {
            if (!isDisplayable() && notifTimer != null) {
                notifTimer.stop();
                notifTimer = null;
            }
        });
    }

    private static int safeLineCount(Path p) {
        try {
            if (p == null || !Files.exists(p)) return 0;
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            return lines == null ? 0 : lines.size();
        } catch (Exception ex) {
            return 0;
        }
    }

    private static String buildNotificationsText(Path p, int tailLines, Role role, String account, DataService data) {
        try {
            if (p == null || !Files.exists(p)) return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (lines == null || lines.isEmpty()) return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");

            int start = Math.max(0, lines.size() - Math.max(1, tailLines));
            List<NotificationItem> items = new ArrayList<NotificationItem>();
            for (int i = start; i < lines.size(); i++) {
                String line = lines.get(i);
                if (!isLineRelevant(line, role, account, data)) continue;
                NotificationItem item = parseNotification(line);
                if (item != null) items.add(item);
            }
            if (items.isEmpty()) return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");
            return renderNotificationsHtml(items);
        } catch (Exception ex) {
            return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");
        }
    }

    private static boolean isLineRelevant(String line, Role role, String account, DataService data) {
        if (line == null || line.trim().isEmpty()) return false;
        String[] parts = line.split("\t", 3);
        if (parts.length < 3) return false;

        String level = parts[1];
        Map<String, String> kv = parseKeyValues(parts[2]);
        String action = valueOr(kv, "action", "");
        String actor = valueOr(kv, "actor", "");
        String applicantId = kv.get("applicantId");
        String jobId = kv.get("jobId");

        if (role == Role.TA) {
            if ("setApplicationStatus".equals(action)) {
                return account != null && account.equals(applicantId);
            }
            if ("createJob".equals(action)) {
                return true;
            }
            return false;
        }

        if (role == Role.MO) {
            if ("submitApplication".equals(action) || "withdrawApplication".equals(action)) {
                if (jobId != null && !jobId.isEmpty() && data != null) {
                    return data.getJob(jobId).map(j -> account != null && account.equals(j.postedBy())).orElse(false);
                }
                return false;
            }
            return false;
        }

        if (role == Role.ADMIN) {
            if ("ERROR".equalsIgnoreCase(level) || "WARN".equalsIgnoreCase(level)) {
                return true;
            }
            return account != null && account.equals(actor);
        }

        return false;
    }

    private static NotificationItem parseNotification(String line) {
        if (line == null) return null;
        String[] parts = line.split("\t", 3);
        if (parts.length < 3) return null;

        String time = formatTime(parts[0]);
        String level = parts[1].trim();
        Map<String, String> kv = parseKeyValues(parts[2]);
        String action = valueOr(kv, "action", "");
        String actor = valueOr(kv, "actor", "System");

        if (action.isEmpty() && ("ERROR".equalsIgnoreCase(level) || "WARN".equalsIgnoreCase(level))) {
            Map<String, String> errorKv = new LinkedHashMap<>();
            errorKv.put("action", level.toLowerCase());
            errorKv.put("rawMessage", parts[2].trim());
            return new NotificationItem(time, level.toLowerCase(), "System", errorKv);
        }

        return new NotificationItem(time, action.isEmpty() ? "activity" : action, actor, kv);
    }

    private static Map<String, String> parseKeyValues(String text) {
        Map<String, String> out = new LinkedHashMap<>();
        if (text == null) return out;
        String[] tokens = text.trim().split("\\s+");
        for (String token : tokens) {
            int idx = token.indexOf('=');
            if (idx <= 0) continue;
            String key = token.substring(0, idx).trim();
            String value = token.substring(idx + 1).trim();
            if (!key.isEmpty()) out.put(key, value);
        }
        return out;
    }

    // ── Rendering ──

    private static String renderNotificationsHtml(List<NotificationItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Sans-Serif;background-color:#f8fafc;padding:10px 12px;'>");
        sb.append("<h2><font color='#1565c0'>").append(I18n.t("notif.header")).append("</font></h2>");
        sb.append("<hr noshade size='1' color='#dddddd'>");
        for (int i = items.size() - 1; i >= 0; i--) {
            NotificationItem item = items.get(i);
            sb.append(renderCard(item));
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String renderCard(NotificationItem item) {
        String action = item.action;
        if ("createJob".equals(action)) return renderCreateJobCard(item);
        if ("setApplicationStatus".equals(action)) return renderStatusChangeCard(item);
        if ("submitApplication".equals(action) || "withdrawApplication".equals(action)) return renderSimpleActionCard(item);
        return renderAdminCard(item);
    }

    private static String renderCardHeader(String actor, String verb, String time) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' cellpadding='0' cellspacing='0'>");
        sb.append("<tr>");
        sb.append("<td><h3><font color='#1565c0'>").append(escapeHtml(actor)).append("</font> ").append(escapeHtml(verb)).append("</h3></td>");
        sb.append("<td align='right' valign='top'><font size='2' color='#999999'>").append(escapeHtml(time)).append("</font></td>");
        sb.append("</tr></table>");
        sb.append("<hr noshade size='1' color='#eeeeee'>");
        return sb.toString();
    }

    private static String renderIdRefs(Map<String, String> kv) {
        List<String> refs = new ArrayList<>();
        String applicationId = kv.get("applicationId");
        String jobId = kv.get("jobId");
        if (applicationId != null && !applicationId.isEmpty()) refs.add("Application: " + shortenId(applicationId));
        if (jobId != null && !jobId.isEmpty()) refs.add("Job: " + shortenId(jobId));
        if (refs.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        sb.append("<br><font size='2' color='#888888'>");
        for (int i = 0; i < refs.size(); i++) {
            if (i > 0) sb.append(" &nbsp; ");
            sb.append(escapeHtml(refs.get(i)));
        }
        sb.append("</font>");
        return sb.toString();
    }

    // ── Card type: createJob ──

    private static String renderCreateJobCard(NotificationItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border:1px solid #dbe4ee;margin:0 0 12px 0;'>");
        sb.append("<tr><td style='padding:10px 14px;'>");
        sb.append(renderCardHeader(item.actor, I18n.t("notif.action.createJob"), item.time));

        String title = item.kv.get("title");
        String hours = item.kv.get("hoursPerWeek");
        String jobId = item.kv.get("jobId");
        String postedBy = item.kv.get("postedBy");

        if (title != null && !title.isEmpty()) {
            sb.append("<b>").append(I18n.t("notif.detail.title")).append(":</b> <font color='#1565c0'><b>").append(escapeHtml(title)).append("</b></font><br>");
        }
        if (hours != null && !hours.isEmpty()) {
            sb.append("<b>").append(I18n.t("notif.detail.hours")).append(":</b> ").append(escapeHtml(hours)).append("<br>");
        }
        if (jobId != null && !jobId.isEmpty()) {
            sb.append("<b>").append(I18n.t("notif.detail.jobId")).append(":</b> ").append(escapeHtml(shortenId(jobId))).append("<br>");
        }
        if (postedBy != null && !postedBy.isEmpty()) {
            sb.append("<b>").append(I18n.t("notif.detail.postedBy")).append(":</b> ").append(escapeHtml(postedBy)).append("<br>");
        }

        sb.append("</td></tr></table>");
        return sb.toString();
    }

    // ── Card type: setApplicationStatus ──

    private static String renderStatusChangeCard(NotificationItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border:1px solid #dbe4ee;margin:0 0 12px 0;'>");
        sb.append("<tr><td style='padding:10px 14px;'>");
        sb.append(renderCardHeader(item.actor, I18n.t("notif.action.setApplicationStatus"), item.time));

        String from = item.kv.get("fromStatus");
        String to = item.kv.get("toStatus");

        sb.append("<table cellpadding='0' cellspacing='0'><tr>");
        if (from != null && !from.isEmpty()) {
            sb.append("<td bgcolor='").append(statusBgColor(from)).append("' style='padding:3px 10px;border:1px solid ").append(statusBorderColor(from)).append(";'>");
            sb.append("<font color='").append(statusTextColor(from)).append("'><b>").append(escapeHtml(I18n.t("notif.status." + from))).append("</b></font></td>");
            sb.append("<td style='padding:0 8px;'>&rarr;</td>");
        }
        if (to != null && !to.isEmpty()) {
            sb.append("<td bgcolor='").append(statusBgColor(to)).append("' style='padding:3px 10px;border:1px solid ").append(statusBorderColor(to)).append(";'>");
            sb.append("<font color='").append(statusTextColor(to)).append("'><b>").append(escapeHtml(I18n.t("notif.status." + to))).append("</b></font></td>");
        }
        sb.append("</tr></table>");

        sb.append(renderIdRefs(item.kv));
        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private static String statusBgColor(String status) {
        if ("ACCEPTED".equals(status)) return "#e8f5e9";
        if ("REJECTED".equals(status)) return "#ffebee";
        return "#e3f2fd";
    }

    private static String statusBorderColor(String status) {
        if ("ACCEPTED".equals(status)) return "#a5d6a7";
        if ("REJECTED".equals(status)) return "#ef9a9a";
        return "#90caf9";
    }

    private static String statusTextColor(String status) {
        if ("ACCEPTED".equals(status)) return "#2e7d32";
        if ("REJECTED".equals(status)) return "#c62828";
        return "#1565c0";
    }

    private static String jobStatusBgColor(String status) {
        if ("COMPLETED".equals(status)) return "#e8f5e9";
        if ("CLOSED".equals(status)) return "#ffebee";
        return "#e3f2fd";
    }

    private static String jobStatusBorderColor(String status) {
        if ("COMPLETED".equals(status)) return "#a5d6a7";
        if ("CLOSED".equals(status)) return "#ef9a9a";
        return "#90caf9";
    }

    private static String jobStatusTextColor(String status) {
        if ("COMPLETED".equals(status)) return "#2e7d32";
        if ("CLOSED".equals(status)) return "#c62828";
        return "#1565c0";
    }

    // ── Card type: submitApplication / withdrawApplication ──

    private static String renderSimpleActionCard(NotificationItem item) {
        boolean isSubmit = "submitApplication".equals(item.action);
        String verb = isSubmit ? I18n.t("notif.action.submitApplication") : I18n.t("notif.action.withdrawApplication");
        String detail = isSubmit ? I18n.t("notif.submit.detail") : I18n.t("notif.withdraw.detail");

        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border:1px solid #dbe4ee;margin:0 0 12px 0;'>");
        sb.append("<tr><td style='padding:10px 14px;'>");
        sb.append(renderCardHeader(item.actor, verb, item.time));
        sb.append("<font color='#333333'>").append(escapeHtml(detail)).append("</font>");
        sb.append(renderIdRefs(item.kv));
        sb.append("</td></tr></table>");
        return sb.toString();
    }

    // ── Card type: admin operations ──

    private static String renderAdminCard(NotificationItem item) {
        StringBuilder sb = new StringBuilder();
        sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border:1px solid #dbe4ee;margin:0 0 12px 0;'>");
        sb.append("<tr><td style='padding:10px 14px;'>");

        String verb = actionVerb(item.action);
        sb.append(renderCardHeader(item.actor, verb, item.time));

        String detail = buildAdminFlowingText(item.action, item.kv);
        sb.append(detail);

        sb.append("</td></tr></table>");
        return sb.toString();
    }

    private static String actionVerb(String action) {
        String key = "notif.action." + action;
        String val = I18n.t(key);
        return val.equals(key) ? I18n.t("notif.admin.performed", action) : val;
    }

    private static String buildAdminFlowingText(String action, Map<String, String> kv) {
        if ("setUserEnabled".equals(action)) {
            String account = kv.get("account");
            String enabled = kv.get("enabled");
            if ("true".equalsIgnoreCase(enabled)) {
                return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.setUserEnabled.enabled", account)) + "</font>";
            }
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.setUserEnabled.disabled", account)) + "</font>";
        }
        if ("deleteUser".equals(action)) {
            String role = kv.get("role");
            String account = kv.get("account");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.deleteUser", role, account)) + "</font>";
        }
        if ("resetPassword".equals(action)) {
            String role = kv.get("role");
            String account = kv.get("account");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.resetPassword", role, account)) + "</font>";
        }
        if ("createMoAccount".equals(action)) {
            String account = kv.get("account");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.createMoAccount", account)) + "</font>";
        }
        if ("export".equals(action)) {
            String type = kv.get("type");
            String format = kv.get("format");
            String dir = kv.get("dir");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.export", type, format, dir)) + "</font>";
        }
        if ("exportWorkloadCsv".equals(action)) {
            String file = kv.get("file");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.exportWorkloadCsv", file)) + "</font>";
        }
        if ("setJobStatus".equals(action)) {
            String jobId = kv.get("jobId");
            String status = kv.get("status");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.setJobStatus", shortenId(jobId))) + " </font>"
                    + "<table cellpadding='0' cellspacing='0' style='display:inline;'><tr><td bgcolor='" + jobStatusBgColor(status) + "' style='padding:2px 8px;border:1px solid " + jobStatusBorderColor(status) + ";'>"
                    + "<font color='" + jobStatusTextColor(status) + "'><b>" + escapeHtml(status) + "</b></font></td></tr></table>";
        }
        if ("setJobCategory".equals(action)) {
            String jobId = kv.get("jobId");
            String category = kv.get("category");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.setJobCategory", shortenId(jobId), category)) + "</font>";
        }
        if ("resetApplicantAiScores".equals(action)) {
            String applicantId = kv.get("applicantId");
            return "<font color='#333333'>" + escapeHtml(I18n.t("notif.admin.resetApplicantAiScores", applicantId)) + "</font>";
        }
        if ("updateConfig".equals(action)) {
            StringBuilder sb = new StringBuilder();
            sb.append("<font color='#333333'>").append(escapeHtml(I18n.t("notif.admin.updateConfig"))).append("</font><br>");
            String dataPath = kv.get("dataPath");
            String pwdLen = kv.get("passwordMinLength");
            String cvFormats = kv.get("cvFormats");
            String lang = kv.get("defaultLang");
            sb.append("<table width='95%' cellpadding='6' cellspacing='0'><tr><td bgcolor='#f5f5f5'>");
            sb.append("<font color='#333333' size='2'>");
            sb.append(escapeHtml(I18n.t("notif.admin.updateConfig.detail",
                    dataPath != null ? dataPath : "-",
                    pwdLen != null ? pwdLen : "-",
                    cvFormats != null ? cvFormats : "-",
                    lang != null ? lang : "-")));
            sb.append("</font></td></tr></table>");
            return sb.toString();
        }
        if ("error".equals(action)) {
            String rawMsg = kv.get("rawMessage");
            return "<font color='#c62828'><b>" + escapeHtml(I18n.t("notif.admin.error")) + ":</b></font> " + escapeHtml(rawMsg);
        }
        if ("warn".equals(action)) {
            String rawMsg = kv.get("rawMessage");
            return "<font color='#e65100'><b>" + escapeHtml(I18n.t("notif.admin.warn")) + ":</b></font> " + escapeHtml(rawMsg);
        }

        // Generic fallback: show key-value pairs in a grey card
        StringBuilder sb = new StringBuilder();
        sb.append("<font color='#333333'>").append(escapeHtml(I18n.t("notif.admin.performed", action))).append("</font>");
        if (!kv.isEmpty()) {
            sb.append("<br><table width='95%' cellpadding='6' cellspacing='0'><tr><td bgcolor='#f5f5f5'>");
            sb.append("<font color='#555555' size='2'>");
            boolean first = true;
            for (Map.Entry<String, String> e : kv.entrySet()) {
                if ("action".equals(e.getKey()) || "actor".equals(e.getKey())) continue;
                if (!first) sb.append(", ");
                sb.append("<b>").append(escapeHtml(e.getKey())).append("</b>: ").append(escapeHtml(e.getValue()));
                first = false;
            }
            sb.append("</font></td></tr></table>");
        }
        return sb.toString();
    }

    private static String wrapNotificationsHtml(String inner) {
        return "<html><body style='font-family:Sans-Serif;background:#f8fafc;padding:10px 12px;'>" + inner + "</body></html>";
    }

    private static String formatTime(String raw) {
        try {
            LocalDateTime dt = LocalDateTime.parse(raw.trim());
            return dt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        } catch (Exception ex) {
            return raw == null ? "" : raw;
        }
    }

    private static String shortenId(String id) {
        if (id == null) return "";
        String v = id.trim();
        if (v.length() <= 12) return v;
        return v.substring(0, 8) + "...";
    }

    private static String valueOr(Map<String, String> kv, String key, String def) {
        String v = kv.get(key);
        return (v == null || v.trim().isEmpty()) ? def : v.trim();
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private static final class NotificationItem {
        private final String time;
        private final String action;
        private final String actor;
        private final Map<String, String> kv;

        private NotificationItem(String time, String action, String actor, Map<String, String> kv) {
            this.time = time == null ? "" : time;
            this.action = action == null ? "" : action;
            this.actor = actor == null ? "" : actor;
            this.kv = kv == null ? new LinkedHashMap<>() : kv;
        }
    }
}
