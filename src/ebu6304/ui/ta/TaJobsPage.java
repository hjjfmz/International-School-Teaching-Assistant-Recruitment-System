package ebu6304.ui.ta;

import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;
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

public final class TaJobsPage extends JPanel {
    private final DataService data;
    private final String account;

    private final DefaultTableModel model;
    private final JTable table;

    public TaJobsPage(DataService data, String account, Runnable onBack) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                I18n.t("ta.jobs.col.id"), I18n.t("ta.jobs.col.title"), I18n.t("ta.jobs.col.skills"), I18n.t("ta.jobs.col.hours"), I18n.t("ta.jobs.col.postedby")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("ta.jobs.title")));
        top.add(new JLabel(I18n.t("ta.jobs.hint")), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton(I18n.t("common.back"));
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton details = new JButton(I18n.t("common.details"));
        JButton apply = new JButton(I18n.t("common.apply"));
        actions.add(backBtn);
        actions.add(refresh);
        actions.add(details);
        actions.add(apply);
        top.add(actions, BorderLayout.EAST);

        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

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
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.job"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(r);
        String jobId = String.valueOf(model.getValueAt(modelRow, 0));
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
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.job"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(r);
        String jobId = String.valueOf(model.getValueAt(modelRow, 0));

        if (data.findApplication(account, jobId).isPresent()) {
            JOptionPane.showMessageDialog(this, I18n.t("ta.jobs.already.applied"));
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, I18n.t("ta.jobs.confirm.apply"), I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        data.submitApplication(account, jobId);
        JOptionPane.showMessageDialog(this, I18n.t("ta.jobs.applied"));
    }
}
