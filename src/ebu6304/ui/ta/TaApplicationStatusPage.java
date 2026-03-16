package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class TaApplicationStatusPage extends JPanel {
    private final DataService data;
    private final String account;

    private final JComboBox<String> filter = new JComboBox<String>(new String[] { "All", "Pending", "Accepted", "Rejected" });
    private final DefaultTableModel model;
    private final JTable table;

    public TaApplicationStatusPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                "Application ID", "Job ID", "Job Title", "Status", "Comment"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Application Status"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Filter:"));
        left.add(filter);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        filter.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        String f = String.valueOf(filter.getSelectedItem());

        for (Application a : data.listApplicationsForApplicant(account)) {
            String status = toEn(a.status());
            if (!"All".equals(f) && !f.equals(status)) continue;

            Job j = data.getJob(a.jobId()).orElse(null);
            String title = j == null ? a.jobId() : j.title();
            model.addRow(new Object[] { a.id(), a.jobId(), title, status, "N/A" });
        }
    }

    private static String toEn(Application.Status st) {
        if (st == Application.Status.ACCEPTED) return "Accepted";
        if (st == Application.Status.REJECTED) return "Rejected";
        return "Pending";
    }
}
