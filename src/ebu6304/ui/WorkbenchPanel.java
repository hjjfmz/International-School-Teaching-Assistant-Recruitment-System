package ebu6304.ui;

import java.awt.BorderLayout;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.Timer;

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
import ebu6304.ui.admin.AdminAiPage;

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
        startOperationLogNotifications(data);

        if (role == Role.TA) {
            TaHomePage home = new TaHomePage(data, account, k -> layout.showContent(k));
            TaProfilePage profile = new TaProfilePage(data, account);
            TaResumePage resume = new TaResumePage(data, account);
            TaJobsPage jobs = new TaJobsPage(data, account, () -> { layout.showContent(I18n.t("nav.ta.home")); layout.setNavSelectedIndex(0); });
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
            MoPostJobPage post = new MoPostJobPage(data, account);
            MoApplicantsPage applicants = new MoApplicantsPage(data, account);
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
            AdminAiPage ai = new AdminAiPage(data);

            layout.addContent(I18n.t("nav.admin.home"), home);
            layout.addContent(I18n.t("nav.admin.users"), users);
            layout.addContent(I18n.t("nav.admin.workload"), workload);
            layout.addContent(I18n.t("nav.admin.jobdata"), jobs);
            layout.addContent(I18n.t("nav.admin.config"), config);
            layout.addContent(I18n.t("nav.admin.export"), export);
            layout.addContent(I18n.t("nav.admin.logs"), logs);
            layout.addContent(I18n.t("nav.admin.ai"), ai);

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
            if (p == null || !Files.exists(p)) return I18n.t("layout.notifications.empty");
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (lines == null || lines.isEmpty()) return I18n.t("layout.notifications.empty");

            int start = Math.max(0, lines.size() - Math.max(1, tailLines));
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < lines.size(); i++) {
                sb.append(lines.get(i)).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            return I18n.t("layout.notifications.empty");
        }
    }
}
