package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class TaJobsPage extends JPanel {
    private final DataService data;
    private final String account;

    private final DefaultTableModel model;
    private final JTable table;

    public TaJobsPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                "Job ID", "Title", "Required skills", "Hours/week", "Posted by"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Job Search"));
        top.add(new JLabel("Select a job and use the buttons on the right"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton details = new JButton("Details");
        JButton apply = new JButton("Apply");
        actions.add(refresh);
        actions.add(details);
        actions.add(apply);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        details.addActionListener(e -> showDetails());
        apply.addActionListener(e -> applySelected());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        List<Job> jobs = data.listJobs();
        for (Job j : jobs) {
            model.addRow(new Object[] {
                    j.id(), j.title(), j.requiredSkills(), Integer.valueOf(j.hoursPerWeek()), j.postedBy()
            });
        }
    }

    private void showDetails() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job");
            return;
        }
        String jobId = String.valueOf(model.getValueAt(r, 0));
        Job j = data.getJob(jobId).orElse(null);
        if (j == null) return;
        String msg = "Title: " + j.title() + "\n" +
                "Hours/week: " + j.hoursPerWeek() + "\n" +
                "Required skills: " + j.requiredSkills() + "\n" +
                "Posted by: " + j.postedBy() + "\n\n" +
                j.description();
        JOptionPane.showMessageDialog(this, msg);
    }

    private void applySelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a job");
            return;
        }
        String jobId = String.valueOf(model.getValueAt(r, 0));

        if (data.findApplication(account, jobId).isPresent()) {
            JOptionPane.showMessageDialog(this, "You have already applied for this job");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, "Apply for the selected job?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        data.submitApplication(account, jobId);
        JOptionPane.showMessageDialog(this, "Application submitted");
    }
}
