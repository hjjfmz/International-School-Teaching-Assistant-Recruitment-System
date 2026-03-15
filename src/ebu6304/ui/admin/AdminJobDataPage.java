package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ebu6304.model.Job;
import ebu6304.storage.DataService;

public final class AdminJobDataPage extends JPanel {
    private final DataService data;
    private final DefaultTableModel model;

    public AdminJobDataPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] { "Job ID", "Posted by", "Title", "Hours/week" }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Job Data"));
        top.add(new JLabel("Mid-fidelity placeholder: job list only"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (Job j : data.listJobs()) {
            model.addRow(new Object[] { j.id(), j.postedBy(), j.title(), Integer.valueOf(j.hoursPerWeek()) });
        }
    }
}
