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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public final class AdminWorkloadPage extends JPanel {
    private final DataService data;
    private final String actor;

    private final JComboBox<String> view = new JComboBox<String>(new String[] { "By TA", "By Category", "By Job" });
    private final JTextField from = new JTextField(10);
    private final JTextField to = new JTextField(10);
    private final JTextField category = new JTextField(10);

    private final DefaultTableModel model;
    private final JTable table;

    public AdminWorkloadPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] { "Key", "Name/Title", "Accepted", "Total hours" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("TA Workload"));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("View:"));
        filters.add(view);
        filters.add(new JLabel("From (yyyy-MM-dd):"));
        filters.add(from);
        filters.add(new JLabel("To (yyyy-MM-dd):"));
        filters.add(to);
        filters.add(new JLabel("Category:"));
        filters.add(category);
        top.add(filters, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton export = new JButton("Export CSV");
        JButton ai = new JButton("AI Balancing (placeholder)");
        actions.add(refresh);
        actions.add(export);
        actions.add(ai);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        export.addActionListener(e -> exportCsv());
        ai.addActionListener(e -> JOptionPane.showMessageDialog(this, "Placeholder: AI workload balancing/ratings will be added later."));

        view.addActionListener(e -> refresh());
        from.addActionListener(e -> refresh());
        to.addActionListener(e -> refresh());
        category.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        if (data == null) return;

        long fromMs = parseDateStart(from.getText().trim());
        long toMs = parseDateEnd(to.getText().trim());
        String catFilter = category.getText().trim().toLowerCase();

        String v = String.valueOf(view.getSelectedItem());
        if ("By Category".equalsIgnoreCase(v)) {
            refreshByCategory(fromMs, toMs, catFilter);
        } else if ("By Job".equalsIgnoreCase(v)) {
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

    private void refreshByCategory(long fromMs, long toMs, String catFilter) {
        Map<String, int[]> agg = new HashMap<String, int[]>();
        for (Job j : data.listJobs()) {
            if (!catFilter.isEmpty() && !j.category().toLowerCase().contains(catFilter)) continue;
            for (Application a : data.listApplicationsForJob(j.id())) {
                if (a.status() != Application.Status.ACCEPTED) continue;
                if (!within(a.createdAt(), fromMs, toMs)) continue;
                String key = j.category() == null || j.category().trim().isEmpty() ? "(empty)" : j.category().trim();
                int[] v = agg.get(key);
                if (v == null) {
                    v = new int[] { 0, 0 };
                    agg.put(key, v);
                }
                v[0] += 1;
                v[1] += j.hoursPerWeek();
            }
        }

        List<String> keys = new ArrayList<String>(agg.keySet());
        Collections.sort(keys, String.CASE_INSENSITIVE_ORDER);
        for (String k : keys) {
            int[] v = agg.get(k);
            model.addRow(new Object[] { k, k, Integer.valueOf(v[0]), Integer.valueOf(v[1]) });
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
        chooser.setDialogTitle("Export Workload CSV");
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
            JOptionPane.showMessageDialog(this, "Export failed");
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
