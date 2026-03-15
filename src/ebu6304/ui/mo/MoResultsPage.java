package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class MoResultsPage extends JPanel {
    private final DataService data;
    private final String account;

    private final JComboBox<JobItem> jobsBox = new JComboBox<JobItem>();
    private final DefaultTableModel model;
    private final JTable table;

    public MoResultsPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] { "Application ID", "TA Account", "Status" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Results"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Job:"));
        left.add(jobsBox);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton send = new JButton("Send notice (placeholder)" );
        actions.add(refresh);
        actions.add(send);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        jobsBox.addActionListener(e -> refresh());
        send.addActionListener(e -> JOptionPane.showMessageDialog(this, "Sent (placeholder): will write notices into ta_info.csv in a later iteration"));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reloadJobs();
        refresh();
    }

    public void reloadJobs() {
        jobsBox.removeAllItems();
        for (Job j : data.listJobs()) {
            if (!account.equals(j.postedBy())) continue;
            jobsBox.addItem(new JobItem(j.id(), j.title()));
        }
    }

    public void refresh() {
        model.setRowCount(0);
        JobItem it = (JobItem) jobsBox.getSelectedItem();
        if (it == null) return;
        for (Application a : data.listApplicationsForJob(it.id)) {
            if (a.status() == Application.Status.SUBMITTED) continue;
            model.addRow(new Object[] { a.id(), a.applicantId(), a.status().name() });
        }
    }

    private static final class JobItem {
        private final String id;
        private final String title;

        private JobItem(String id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
