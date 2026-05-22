package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.storage.OperationLog;
import ebu6304.ui.I18n;
import ebu6304.ui.UiTheme;

public final class AdminWorkloadPage extends JPanel {
    private final DataService data;
    private final String actor;

    private final JComboBox<String> view = new JComboBox<String>(new String[] { "By TA", "By Job" });
    private final JTextField from = new JTextField(10);
    private final JTextField to = new JTextField(10);
    private final JTextField category = new JTextField(10);

    private final DefaultTableModel model;
    private final JTable table;

    public AdminWorkloadPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        UiTheme.stylePage(this);

        model = new DefaultTableModel(new Object[] { I18n.t("admin.workload.col.key"), I18n.t("admin.workload.col.name"), I18n.t("admin.workload.col.accepted"), I18n.t("admin.workload.col.hours") }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        UiTheme.styleTable(table);
        UiTheme.styleCombo(view);
        UiTheme.styleTextField(from);
        UiTheme.styleTextField(to);
        UiTheme.styleTextField(category);
        from.setPreferredSize(new java.awt.Dimension(116, 36));
        to.setPreferredSize(new java.awt.Dimension(116, 36));
        category.setPreferredSize(new java.awt.Dimension(132, 36));

        JPanel top = new JPanel(new BorderLayout(0, 8));
        UiTheme.stylePanelCard(top, I18n.t("admin.workload.title"));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filters.setOpaque(false);
        filters.add(new JLabel(I18n.t("admin.workload.view")));
        filters.add(view);
        filters.add(new JLabel(I18n.t("admin.workload.from")));
        filters.add(from);
        filters.add(new JLabel(I18n.t("admin.workload.to")));
        filters.add(to);
        filters.add(new JLabel(I18n.t("admin.workload.category")));
        filters.add(category);
        top.add(filters, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton export = new JButton(I18n.t("common.exportcsv"));
        JButton ai = new JButton(I18n.t("admin.workload.ai"));
        UiTheme.styleButton(refresh);
        UiTheme.styleButton(export);
        UiTheme.stylePrimaryButton(ai);
        actions.add(refresh);
        actions.add(export);
        actions.add(ai);
        top.add(actions, BorderLayout.SOUTH);

        refresh.addActionListener(e -> refresh());
        export.addActionListener(e -> exportCsv());
        ai.addActionListener(e -> JOptionPane.showMessageDialog(this, I18n.t("admin.workload.ai.msg")));

        view.addActionListener(e -> refresh());
        from.addActionListener(e -> refresh());
        to.addActionListener(e -> refresh());
        category.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(table);
        UiTheme.styleScrollPane(sp);
        add(sp, BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        if (data == null) return;

        long fromMs = parseDateStart(from.getText().trim());
        long toMs = parseDateEnd(to.getText().trim());
        String catFilter = category.getText().trim().toLowerCase();

        String v = String.valueOf(view.getSelectedItem());
        if ("By Job".equalsIgnoreCase(v)) {
            refreshByJob(fromMs, toMs, catFilter);
        } else {
            refreshByTa(fromMs, toMs, catFilter);
        }
    }

    private void refreshByTa(long fromMs, long toMs, String catFilter) {
        List<Applicant> tas = data.listApplicants();
        for (Applicant ta : tas) {
            int accepted = 0;
            int hours = 0;
            for (Application a : data.listApplicationsForApplicant(ta.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                if (!within(a.createdAt(), fromMs, toMs)) continue;
                Job j = data.getJob(a.jobId()).orElse(null);
                if (j == null) continue;
                if (!catFilter.isEmpty() && !j.category().toLowerCase().contains(catFilter)) continue;
                accepted++;
                hours += j.hoursPerWeek();
            }
            model.addRow(new Object[] { ta.id(), ta.name(), Integer.valueOf(accepted), Integer.valueOf(hours) });
        }
    }

    private void refreshByJob(long fromMs, long toMs, String catFilter) {
        List<Job> jobs = data.listJobs();
        Collections.sort(jobs, new Comparator<Job>() {
            @Override
            public int compare(Job o1, Job o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.title(), o2.title());
            }
        });

        for (Job j : jobs) {
            if (!catFilter.isEmpty() && !j.category().toLowerCase().contains(catFilter)) continue;
            int accepted = 0;
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                if (!within(a.createdAt(), fromMs, toMs)) continue;
                accepted++;
            }
            int hours = accepted * j.hoursPerWeek();
            model.addRow(new Object[] { j.id(), j.title(), Integer.valueOf(accepted), Integer.valueOf(hours) });
        }
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.t("admin.workload.export.title"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        List<String> lines = new ArrayList<String>();
        lines.add("key,nameOrTitle,accepted,totalHours");
        for (int i = 0; i < model.getRowCount(); i++) {
            lines.add(ebu6304.storage.Csv.join(
                    String.valueOf(model.getValueAt(i, 0)),
                    String.valueOf(model.getValueAt(i, 1)),
                    String.valueOf(model.getValueAt(i, 2)),
                    String.valueOf(model.getValueAt(i, 3))
            ));
        }

        try {
            Files.write(chooser.getSelectedFile().toPath(), lines, StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            OperationLog.append(data.tempOperationFile(), "INFO", "actor=" + actor + " action=exportWorkloadCsv file=" + chooser.getSelectedFile().getAbsolutePath());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.export.failed"));
        }
    }

    private static boolean within(long t, long fromMs, long toMs) {
        if (fromMs > 0 && t < fromMs) return false;
        if (toMs > 0 && t > toMs) return false;
        return true;
    }

    private static long parseDateStart(String text) {
        if (text == null || text.trim().isEmpty()) return 0L;
        try {
            LocalDate d = LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return d.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (DateTimeParseException ex) {
            return 0L;
        }
    }

    private static long parseDateEnd(String text) {
        if (text == null || text.trim().isEmpty()) return 0L;
        try {
            LocalDate d = LocalDate.parse(text.trim(), DateTimeFormatter.ISO_LOCAL_DATE);
            return d.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1;
        } catch (DateTimeParseException ex) {
            return 0L;
        }
    }
}


