package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ebu6304.storage.DataService;

public final class AdminHomePage extends JPanel {
    public interface Nav {
        void go(String key);
    }

    private final JLabel stats = new JLabel(" ");

    public AdminHomePage(DataService data, Nav nav) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JLabel title = new JLabel("System Overview");
        add(title, BorderLayout.NORTH);

        JPanel quick = new JPanel(new GridLayout(1, 3, 10, 10));
        JButton usersBtn = new JButton("User Management");
        JButton exportBtn = new JButton("Data Export");
        JButton configBtn = new JButton("System Config");
        quick.add(usersBtn);
        quick.add(exportBtn);
        quick.add(configBtn);
        add(quick, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setBorder(BorderFactory.createTitledBorder("Stats"));
        bottom.add(stats, BorderLayout.CENTER);
        add(bottom, BorderLayout.SOUTH);

        usersBtn.addActionListener(e -> { if (nav != null) nav.go("User Management"); });
        exportBtn.addActionListener(e -> { if (nav != null) nav.go("Data Export"); });
        configBtn.addActionListener(e -> { if (nav != null) nav.go("System Config"); });

        refresh(data);
    }

    public void refresh(DataService data) {
        int taCount = data.listApplicants().size();
        int userCount = data.listUsers().size();
        int jobCount = data.listJobs().size();
        stats.setText("TA registered: " + taCount + "   Accounts: " + userCount + "   Jobs: " + jobCount);
    }
}
