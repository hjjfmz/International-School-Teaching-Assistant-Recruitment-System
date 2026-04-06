package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class AdminJobDataPage extends JPanel {
    private final DataService data;
    private final String actor;

    private final JTextField keyword = new JTextField(10);
    private final JTextField postedBy = new JTextField(8);
    private final JComboBox<String> status = new JComboBox<String>(new String[] { "All", "OPEN", "CLOSED", "COMPLETED" });
    private final JTextField category = new JTextField(8);

    private final DefaultTableModel model;
    private final JTable table;

    public AdminJobDataPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] { I18n.t("admin.jobdata.col.id"), I18n.t("admin.jobdata.col.postedby"), I18n.t("admin.jobdata.col.title"), I18n.t("admin.jobdata.col.hours"), I18n.t("admin.jobdata.col.status"), I18n.t("admin.jobdata.col.category"), I18n.t("admin.jobdata.col.apps"), I18n.t("admin.jobdata.col.accepted") }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("admin.jobdata.title")));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.add(new JLabel(I18n.t("admin.jobdata.keyword")));
        filters.add(keyword);
        filters.add(new JLabel(I18n.t("admin.jobdata.postedby")));
        filters.add(postedBy);
        filters.add(new JLabel(I18n.t("admin.jobdata.status")));
        filters.add(status);
        filters.add(new JLabel(I18n.t("admin.jobdata.category")));
        filters.add(category);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton forceClose = new JButton(I18n.t("admin.jobdata.forceclose"));
        JButton complete = new JButton(I18n.t("admin.jobdata.complete"));
        JButton setCategory = new JButton(I18n.t("admin.jobdata.setcategory"));
        actions.add(refresh);
        actions.add(forceClose);
        actions.add(complete);
        actions.add(setCategory);

        JPanel topContent = new JPanel(new BorderLayout(10, 5));
        topContent.add(filters, BorderLayout.NORTH);
        topContent.add(actions, BorderLayout.SOUTH);
        top.add(topContent, BorderLayout.CENTER);

        refresh.addActionListener(e -> refresh());
        keyword.addActionListener(e -> refresh());
        postedBy.addActionListener(e -> refresh());
        status.addActionListener(e -> refresh());
        category.addActionListener(e -> refresh());

        forceClose.addActionListener(e -> setStatusSelected(Job.Status.CLOSED));
        complete.addActionListener(e -> setStatusSelected(Job.Status.COMPLETED));
        setCategory.addActionListener(e -> setCategorySelected());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);

        String kw = keyword.getText().trim().toLowerCase();
        String pb = postedBy.getText().trim().toLowerCase();
        String st = String.valueOf(status.getSelectedItem());
        String cat = category.getText().trim().toLowerCase();

        for (Job j : data.listJobs()) {
            if (!kw.isEmpty()) {
                String hay = (j.id() + " " + j.title() + " " + j.requiredSkills() + " " + j.description()).toLowerCase();
                if (!hay.contains(kw)) continue;
            }
            if (!pb.isEmpty() && !j.postedBy().toLowerCase().contains(pb)) continue;
            if (!cat.isEmpty() && !j.category().toLowerCase().contains(cat)) continue;
            if (!"All".equalsIgnoreCase(st) && !j.status().name().equalsIgnoreCase(st)) continue;

            List<Application> apps = data.listApplicationsForJob(j.id());
            int accepted = 0;
            for (Application a : apps) {
                if (a.status() == Application.Status.ACCEPTED) accepted++;
            }

            model.addRow(new Object[] {
                    j.id(), j.postedBy(), j.title(), Integer.valueOf(j.hoursPerWeek()), j.status().name(), j.category(), Integer.valueOf(apps.size()), Integer.valueOf(accepted)
            });
        }
    }

    private void setStatusSelected(Job.Status newStatus) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.job"));
            return;
        }
        String jobId = String.valueOf(model.getValueAt(r, 0));
        boolean ok = data.setJobStatus(actor, jobId, newStatus);
        if (!ok) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.operation.failed"));
            return;
        }
        refresh();
    }

    private void setCategorySelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.job"));
            return;
        }
        String jobId = String.valueOf(model.getValueAt(r, 0));
        String current = String.valueOf(model.getValueAt(r, 5));
        String input = JOptionPane.showInputDialog(this, I18n.t("admin.jobdata.categoryprompt"), current);
        if (input == null) return;
        boolean ok = data.setJobCategory(actor, jobId, input.trim());
        if (!ok) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.operation.failed"));
            return;
        }
        refresh();
    }
}