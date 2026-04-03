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

public final class WorkbenchPanel extends JPanel {
    private final AppLayout layout;
    private Timer notifTimer;
    private int lastReadLineCount;
    private int lastSeenLineCount;

    public WorkbenchPanel(DataService data, Role role, String account, Runnable logout) {
        super(new BorderLayout());

        String[] nav;
        if (role == Role.TA) {
            nav = new String[] {
                "TA Home",
                "Profile",
                "Resume",
                "Job Search",
                "My Applications",
                "Application Status"
            };
        } else if (role == Role.MO) {
            nav = new String[] {
                "MO Home",
                "Post Job",
                "Applicants",
                "Results",
                "My Posts"
            };
        } else {
            nav = new String[] {
                "Admin Home",
                "User Management",
                "TA Workload",
                "Job Data",
                "System Config",
                "Data Export",
                "Operation Logs"
            };
        }

        final AppLayout[] holder = new AppLayout[1];
        holder[0] = new AppLayout(role, nav, () -> {
            if (logout != null) logout.run();
        }, key -> {
            if (key == null) return;
            holder[0].showContent(key);
        });
        layout = holder[0];

        layout.setUser(role, account);
        startOperationLogNotifications(data);

        if (role == Role.TA) {
            TaHomePage home = new TaHomePage(data, account, k -> layout.showContent(k));
            TaProfilePage profile = new TaProfilePage(data, account);
            TaResumePage resume = new TaResumePage(data, account);
            TaJobsPage jobs = new TaJobsPage(data, account, () -> { layout.showContent("TA Home"); layout.setNavSelectedIndex(0); });
            TaMyApplicationsPage myApps = new TaMyApplicationsPage(data, account);
            TaApplicationStatusPage status = new TaApplicationStatusPage(data, account, () -> { layout.showContent("TA Home"); layout.setNavSelectedIndex(0); });

            layout.addContent("TA Home", home);
            layout.addContent("Profile", profile);
            layout.addContent("Resume", resume);
            layout.addContent("Job Search", jobs);
            layout.addContent("My Applications", myApps);
            layout.addContent("Application Status", status);

            layout.showContent("TA Home");
            layout.setNavSelectedIndex(0);
        } else if (role == Role.MO) {
            MoHomePage home = new MoHomePage(data, account, k -> layout.showContent(k));
            MoPostJobPage post = new MoPostJobPage(data, account);
            MoApplicantsPage applicants = new MoApplicantsPage(data, account);
            MoResultsPage results = new MoResultsPage(data, account);
            MoMyPostsPage myPosts = new MoMyPostsPage(data, account);

            layout.addContent("MO Home", home);
            layout.addContent("Post Job", post);
            layout.addContent("Applicants", applicants);
            layout.addContent("Results", results);
            layout.addContent("My Posts", myPosts);

            layout.showContent("MO Home");
            layout.setNavSelectedIndex(0);
        } else {
            AdminHomePage home = new AdminHomePage(data, k -> layout.showContent(k));
            AdminUserManagementPage users = new AdminUserManagementPage(data, account);
            AdminWorkloadPage workload = new AdminWorkloadPage(data, account);
            AdminJobDataPage jobs = new AdminJobDataPage(data, account);
            AdminConfigPage config = new AdminConfigPage(data, account);
            AdminExportPage export = new AdminExportPage(data, account);
            AdminLogPage logs = new AdminLogPage(data);

            layout.addContent("Admin Home", home);
            layout.addContent("User Management", users);
            layout.addContent("TA Workload", workload);
            layout.addContent("Job Data", jobs);
            layout.addContent("System Config", config);
            layout.addContent("Data Export", export);
            layout.addContent("Operation Logs", logs);

            layout.showContent("Admin Home");
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
            if (p == null || !Files.exists(p)) return "No notifications.";
            List<String> lines = Files.readAllLines(p, StandardCharsets.UTF_8);
            if (lines == null || lines.isEmpty()) return "No notifications.";

            int start = Math.max(0, lines.size() - Math.max(1, tailLines));
            StringBuilder sb = new StringBuilder();
            for (int i = start; i < lines.size(); i++) {
                sb.append(lines.get(i)).append("\n");
            }
            return sb.toString();
        } catch (Exception ex) {
            return "Unable to load notifications.";
        }
    }
}
