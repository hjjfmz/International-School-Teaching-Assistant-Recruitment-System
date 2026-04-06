package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class TaMyApplicationsPage extends JPanel {
    private final DataService data;
    private final String account;

    private final DefaultTableModel model;
    private final JTable table;

    public TaMyApplicationsPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                I18n.t("ta.myapps.col.appid"), I18n.t("ta.myapps.col.jobid"), I18n.t("ta.myapps.col.jobtitle"), I18n.t("ta.myapps.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("ta.myapps.title")));
        top.add(new JLabel(I18n.t("ta.myapps.hint")), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton withdraw = new JButton(I18n.t("common.withdraw"));
        JButton export = new JButton(I18n.t("common.exportcsv"));
        actions.add(refresh);
        actions.add(withdraw);
        actions.add(export);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        withdraw.addActionListener(e -> withdrawSelected());
        export.addActionListener(e -> exportCsv());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (Application a : data.listApplicationsForApplicant(account)) {
            Job j = data.getJob(a.jobId()).orElse(null);
            String title = j == null ? a.jobId() : j.title();
            model.addRow(new Object[] { a.id(), a.jobId(), title, a.status().name() });
        }
    }

    private void withdrawSelected() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.application"));
            return;
        }
        int modelRow = table.convertRowIndexToModel(r);
        String status = String.valueOf(model.getValueAt(modelRow, 3));
        if (!"SUBMITTED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, I18n.t("ta.myapps.cannot.withdraw"));
            return;
        }
        String jobId = String.valueOf(model.getValueAt(modelRow, 1));
        int ok = JOptionPane.showConfirmDialog(this, I18n.t("ta.myapps.confirm.withdraw"), I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean done = data.withdrawApplication(account, jobId);
        if (!done) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.withdraw.failed"));
            return;
        }
        JOptionPane.showMessageDialog(this, I18n.t("msg.withdrawn"));
        refresh();
    }

    private void exportCsv() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export CSV");
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        List<String> lines = new ArrayList<String>();
        lines.add("applicationId,jobId,jobTitle,status");
        for (Application a : data.listApplicationsForApplicant(account)) {
            Job j = data.getJob(a.jobId()).orElse(null);
            String title = j == null ? a.jobId() : j.title();
            lines.add(ebu6304.storage.Csv.join(a.id(), a.jobId(), title, a.status().name()));
        }

        try {
            Files.write(chooser.getSelectedFile().toPath(), lines, StandardCharsets.UTF_8);
            JOptionPane.showMessageDialog(this, I18n.t("msg.export.success"));
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.export.failed"));
        }
    }
}
