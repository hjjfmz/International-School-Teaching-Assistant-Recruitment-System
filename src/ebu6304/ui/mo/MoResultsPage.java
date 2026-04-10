package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.HierarchyEvent;

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
import ebu6304.ui.I18n;

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

        model = new DefaultTableModel(new Object[] { I18n.t("mo.results.col.appid"), I18n.t("mo.results.col.taaccount"), I18n.t("mo.results.col.status") }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("mo.results.title")));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel(I18n.t("common.job")));
        left.add(jobsBox);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton send = new JButton(I18n.t("mo.results.send"));
        actions.add(refresh);
        actions.add(send);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        jobsBox.addActionListener(e -> refresh());
        send.addActionListener(e -> JOptionPane.showMessageDialog(this, "Sent (placeholder): will write notices into ta_info.csv in a later iteration"));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refreshView();
            }
        });

        refreshView();
    }

    public void reloadJobs() {
        String selectedId = selectedJobId();
        jobsBox.removeAllItems();
        JobItem preferred = null;
        JobItem fallback = null;
        for (Job j : data.listJobs()) {
            if (!account.equals(j.postedBy())) continue;
            JobItem item = new JobItem(j.id(), j.title());
            jobsBox.addItem(item);

            if (selectedId != null && selectedId.equals(j.id())) {
                preferred = item;
            } else if (fallback == null && hasProcessedResults(j.id())) {
                fallback = item;
            }
        }

        if (preferred != null) {
            jobsBox.setSelectedItem(preferred);
        } else if (fallback != null) {
            jobsBox.setSelectedItem(fallback);
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

    public void refreshView() {
        reloadJobs();
        refresh();
    }

    private String selectedJobId() {
        JobItem selected = (JobItem) jobsBox.getSelectedItem();
        return selected == null ? null : selected.id;
    }

    private boolean hasProcessedResults(String jobId) {
        if (jobId == null) return false;
        for (Application a : data.listApplicationsForJob(jobId)) {
            if (a.status() != Application.Status.SUBMITTED) {
                return true;
            }
        }
        return false;
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
