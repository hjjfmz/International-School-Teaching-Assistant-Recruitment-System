package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class MoApplicantsPage extends JPanel {
    private final DataService data;
    private final String account;

    private final JComboBox<JobItem> jobsBox = new JComboBox<JobItem>();
    private final DefaultTableModel model;
    private final JTable table;

    public MoApplicantsPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                "Application ID", "TA Account", "TA Name", "Email", "CV path", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Applicants"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Job:"));
        left.add(jobsBox);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton accept = new JButton("Accept");
        JButton reject = new JButton("Reject");
        JButton openCv = new JButton("Open CV");
        actions.add(refresh);
        actions.add(openCv);
        actions.add(accept);
        actions.add(reject);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        jobsBox.addActionListener(e -> refresh());
        accept.addActionListener(e -> setStatusSelected(Application.Status.ACCEPTED));
        reject.addActionListener(e -> setStatusSelected(Application.Status.REJECTED));
        openCv.addActionListener(e -> openCv());

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

        List<Application> apps = data.listApplicationsForJob(it.id);
        for (Application a : apps) {
            Applicant ta = data.getApplicant(a.applicantId()).orElse(null);
            model.addRow(new Object[] {
                    a.id(), a.applicantId(), ta == null ? "" : ta.name(), ta == null ? "" : ta.email(), ta == null ? "" : ta.cvPath(), a.status().name()
            });
        }
    }

    private void setStatusSelected(Application.Status st) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant");
            return;
        }
        String appId = String.valueOf(model.getValueAt(r, 0));
        data.setApplicationStatus(appId, st);
        JOptionPane.showMessageDialog(this, "Updated");
        refresh();
    }

    private void openCv() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant");
            return;
        }
        String path = String.valueOf(model.getValueAt(r, 4));
        if (path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No CV path");
            return;
        }
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(path));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open file");
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
