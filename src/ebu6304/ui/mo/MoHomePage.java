package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class MoHomePage extends JPanel {
    public interface Nav {
        void go(String key);
    }

    private final JLabel statsLabel = new JLabel(" ");

    public MoHomePage(DataService data, String account, Nav nav) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel welcome = new JLabel("Hi " + account + ", welcome to the TA recruitment system");
        add(welcome, BorderLayout.NORTH);

        JPanel quick = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton postBtn = new JButton("Post a new job");
        JButton pendingBtn = new JButton("Pending applicants");
        quick.add(postBtn);
        quick.add(pendingBtn);
        add(quick, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Stats"));
        bottom.add(statsLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        postBtn.addActionListener(e -> {
            if (nav != null) nav.go("Post Job");
        });
        pendingBtn.addActionListener(e -> {
            if (nav != null) nav.go("Applicants");
        });

        refreshStats(data, account);
    }

    public void refreshStats(DataService data, String account) {
        int jobs = 0;
        int pending = 0;
        int accepted = 0;
        for (Job j : data.listJobs()) {
            if (!account.equals(j.postedBy())) continue;
            jobs++;
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() == Application.Status.SUBMITTED) pending++;
                if (a.status() == Application.Status.ACCEPTED) accepted++;
            }
        }
        statsLabel.setText("Jobs posted: " + jobs + "   Pending applicants: " + pending + "   Accepted: " + accepted);
    }
}
