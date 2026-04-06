package ebu6304.ui.mo;

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

public final class MoMyPostsPage extends JPanel {
    private final DataService data;
    private final String account;

    private final DefaultTableModel model;
    private final JTable table;

    public MoMyPostsPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] { "Job ID", "Title", "Hours/week", "Required skills" }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("My Posts"));

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        actions.add(refresh);
        top.add(new JLabel("Showing jobs posted by you"), BorderLayout.WEST);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        for (Job j : data.listJobs()) {
            if (!account.equals(j.postedBy())) continue;
            model.addRow(new Object[] { j.id(), j.title(), Integer.valueOf(j.hoursPerWeek()), j.requiredSkills() });
        }
    }
}
