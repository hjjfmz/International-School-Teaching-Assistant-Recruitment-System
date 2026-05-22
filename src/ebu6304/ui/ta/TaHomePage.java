package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ebu6304.model.Application;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class TaHomePage extends JPanel {
    public interface Nav {
        void go(String key);
    }

    private final JLabel statsLabel = new JLabel(" ");

    public TaHomePage(DataService data, String account, Nav nav) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel welcome = new JLabel(I18n.t("ta.home.welcome", account));
        add(welcome, BorderLayout.NORTH);

        JPanel quick = new JPanel(new GridLayout(1, 2, 10, 10));
        JButton jobsBtn = new JButton(I18n.t("ta.home.browsejobs"));
        JButton statusBtn = new JButton(I18n.t("ta.home.mystatus"));
        quick.add(jobsBtn);
        quick.add(statusBtn);
        add(quick, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder(I18n.t("ta.home.stats")));
        bottom.add(statsLabel, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        jobsBtn.addActionListener(e -> {
            if (nav != null) nav.go(I18n.t("nav.ta.jobs"));
        });
        statusBtn.addActionListener(e -> {
            if (nav != null) nav.go(I18n.t("nav.ta.status"));
        });

        refreshStats(data, account);
    }

    public void refreshStats(DataService data, String account) {
        if (data == null || account == null) return;
        int total = 0;
        int submitted = 0;
        int accepted = 0;
        int rejected = 0;
        for (Application a : data.listApplicationsForApplicant(account)) {
            total++;
            if (a.status() == Application.Status.SUBMITTED) submitted++;
            if (a.status() == Application.Status.ACCEPTED) accepted++;
            if (a.status() == Application.Status.REJECTED) rejected++;
        }
        statsLabel.setText(I18n.t("ta.home.stats.text", total, submitted, accepted, rejected));
    }
}
