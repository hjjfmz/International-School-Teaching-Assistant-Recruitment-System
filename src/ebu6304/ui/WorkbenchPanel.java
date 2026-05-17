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
import ebu6304.ui.admin.AdminWorkloadPage;

public final class WorkbenchPanel extends JPanel {
    private final AppLayout layout;
    private Timer notifTimer;
    private int lastReadLineCount;
    private int lastSeenLineCount;

    public WorkbenchPanel(DataService data, Role role, String account, Runnable logout, Runnable onLanguageChange) {
        super(new BorderLayout());

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
                I18n.t("nav.admin.logs")
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
        startOperationLogNotifications(data);
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
            layout.addContent(I18n.t("nav.admin.logs"), logs);

            layout.showContent(I18n.t("nav.admin.home"));
            layout.setNavSelectedIndex(0);
        }

        add(layout, BorderLayout.CENTER);
    }

    private void startOperationLogNotifications(DataService data) {
        if (data == null) return;
        final Path log = data.tempOperationFile();
        lastReadLineCount = safeLineCount(log);
        lastSeenLineCount = lastReadLineCount;
        layout.setUnreadNotifications(0);

        layout.setNotificationsTextSupplier(() -> buildNotificationsText(log, 200));

        layout.setOnNotificationsOpened(() -> {
            lastReadLineCount = safeLineCount(log);
            lastSeenLineCount = lastReadLineCount;
            layout.setUnreadNotifications(0);
        });

        notifTimer = new Timer(2000, e -> {
            int lines = safeLineCount(log);
            if (lines > lastSeenLineCount) lastSeenLineCount = lines;
            int unread = Math.max(0, lastSeenLineCount - lastReadLineCount);
            layout.setUnreadNotifications(unread);
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

    private static long safeSize(Path p) {
        try {
            if (p == null || !Files.exists(p)) return 0L;
            return Files.size(p);
        } catch (Exception ex) {
            return 0L;
        }
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

    private static String buildNotificationsText(Path p, int tailLines) {
        try {
            if (p == null || !Files.exists(p)) return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (lines == null || lines.isEmpty()) return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");

            int start = Math.max(0, lines.size() - Math.max(1, tailLines));
            List<NotificationItem> items = new ArrayList<NotificationItem>();
            for (int i = start; i < lines.size(); i++) {
                NotificationItem item = parseNotification(lines.get(i));
                if (item != null) items.add(item);
            }
            if (items.isEmpty()) return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");
            return renderNotificationsHtml(items);
        } catch (Exception ex) {
            return wrapNotificationsHtml("<div style='color:#64748b;padding:12px 0;'>" + escapeHtml(I18n.t("layout.notifications.empty")) + "</div>");
        }
    }

    private static NotificationItem parseNotification(String line) {
        if (line == null) return null;
        String[] parts = line.split("\t", 3);
        if (parts.length < 3) return null;

        Map<String, String> kv = parseKeyValues(parts[2]);
        String action = valueOr(kv, "action", "activity");
        String actor = valueOr(kv, "actor", "System");
        String time = formatTime(parts[0]);
        String title = buildTitle(action, actor);
        String meta = buildMeta(parts[1], kv);
        String detail = buildDetail(action, kv);
        return new NotificationItem(time, title, meta, detail);
    }

    private static Map<String, String> parseKeyValues(String text) {
        Map<String, String> out = new LinkedHashMap<String, String>();
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

    private static String buildTitle(String action, String actor) {
        if ("submitApplication".equals(action)) return actor + " submitted an application";
        if ("setApplicationStatus".equals(action)) return actor + " updated an application status";
        if ("withdrawApplication".equals(action)) return actor + " withdrew an application";
        if ("createJob".equals(action)) return actor + " posted a new job";
        if ("setJobStatus".equals(action)) return actor + " changed a job status";
        return actor + " performed " + action;
    }

    private static String buildMeta(String level, Map<String, String> kv) {
        List<String> bits = new ArrayList<String>();
        if (level != null && !level.trim().isEmpty()) bits.add(level.trim());
        String applicantId = kv.get("applicantId");
        String applicationId = kv.get("applicationId");
        String jobId = kv.get("jobId");
        if (applicantId != null && !applicantId.isEmpty()) bits.add("Applicant " + applicantId);
        if (applicationId != null && !applicationId.isEmpty()) bits.add("Application " + shortenId(applicationId));
        if (jobId != null && !jobId.isEmpty()) bits.add("Job " + shortenId(jobId));
        return joinBits(bits);
    }

    private static String buildDetail(String action, Map<String, String> kv) {
        if ("setApplicationStatus".equals(action)) {
            String from = kv.get("fromStatus");
            String to = kv.get("toStatus");
            if (from != null && to != null) return "Status changed from <b>" + escapeHtml(from) + "</b> to <b>" + escapeHtml(to) + "</b>.";
            if (to != null) return "Status updated to <b>" + escapeHtml(to) + "</b>.";
        }
        if ("submitApplication".equals(action)) {
            return "A new candidate application was submitted for review.";
        }
        if ("withdrawApplication".equals(action)) {
            return "The candidate withdrew this application.";
        }
        if ("createJob".equals(action)) {
            String hours = kv.get("hoursPerWeek");
            String title = kv.get("title");
            List<String> bits = new ArrayList<String>();
            if (title != null && !title.isEmpty()) bits.add("Title: <b>" + escapeHtml(title) + "</b>");
            if (hours != null && !hours.isEmpty()) bits.add("Hours/week: <b>" + escapeHtml(hours) + "</b>");
            return bits.isEmpty() ? "A new job posting was created." : joinBits(bits) + ".";
        }
        String raw = kv.isEmpty() ? "" : kv.toString();
        return raw.isEmpty() ? "Activity recorded." : escapeHtml(raw);
    }

    private static String renderNotificationsHtml(List<NotificationItem> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("<html><body style='font-family:Sans-Serif;background-color:#f8fafc;padding:10px 12px;'>");
        sb.append("<div style='color:#0f172a;font-size:16px;font-weight:bold;padding:4px 2px 12px 2px;'>Recent activity</div>");
        for (int i = items.size() - 1; i >= 0; i--) {
            NotificationItem item = items.get(i);
            sb.append("<table width='100%' cellpadding='0' cellspacing='0' style='background-color:#ffffff;border:1px solid #dbe4ee;margin:0 0 10px 0;'>");
            sb.append("<tr><td style='padding:10px 12px;'>");
            sb.append("<table width='100%' cellpadding='0' cellspacing='0'>");
            sb.append("<tr>");
            sb.append("<td style='color:#0f172a;font-size:13px;font-weight:bold;'>").append(escapeHtml(item.title)).append("</td>");
            sb.append("<td align='right' style='color:#64748b;font-size:12px;'>").append(escapeHtml(item.time)).append("</td>");
            sb.append("</tr></table>");
            if (!item.meta.isEmpty()) {
                sb.append("<div style='color:#475569;font-size:12px;padding-top:4px;'>").append(escapeHtml(item.meta)).append("</div>");
            }
            if (!item.detail.isEmpty()) {
                sb.append("<div style='color:#1e293b;font-size:13px;padding-top:8px;'>").append(item.detail).append("</div>");
            }
            sb.append("</td></tr></table>");
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    private static String wrapNotificationsHtml(String inner) {
        return "<html><body style='font-family:Sans-Serif;background:#f8fafc;padding:10px 12px;'>" + inner + "</body></html>";
    }

    private static String joinBits(List<String> bits) {
        StringBuilder sb = new StringBuilder();
        for (String bit : bits) {
            if (bit == null || bit.isEmpty()) continue;
            if (sb.length() > 0) sb.append("  |  ");
            sb.append(bit);
        }
        return sb.toString();
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
        private final String title;
        private final String meta;
        private final String detail;

        private NotificationItem(String time, String title, String meta, String detail) {
            this.time = time == null ? "" : time;
            this.title = title == null ? "" : title;
            this.meta = meta == null ? "" : meta;
            this.detail = detail == null ? "" : detail;
        }
    }
}
