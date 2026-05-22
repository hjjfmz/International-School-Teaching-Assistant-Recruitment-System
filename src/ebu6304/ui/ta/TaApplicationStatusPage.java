package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FlowLayout;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class TaApplicationStatusPage extends JPanel {
    private final DataService data;
    private final String account;

    private final JComboBox<String> filter = new JComboBox<String>(new String[] { "All", "Pending", "Accepted", "Rejected" });
    private final DefaultTableModel model;
    private final JTable table;

    public TaApplicationStatusPage(DataService data, String account, Runnable onBack) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                I18n.t("ta.status.col.appid"), I18n.t("ta.status.col.jobid"), I18n.t("ta.status.col.jobtitle"), I18n.t("ta.status.col.status"), I18n.t("ta.status.col.comment")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        styleTable(table);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("ta.status.title")));
        top.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);
        left.add(new JLabel(I18n.t("common.filter")));
        styleCombo(filter);
        left.add(filter);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton backBtn = new JButton(I18n.t("common.back"));
        JButton refresh = new JButton(I18n.t("common.refresh"));
        styleActionButton(backBtn);
        styleActionButton(refresh);
        actions.add(backBtn);
        actions.add(refresh);
        top.add(actions, BorderLayout.EAST);

        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });

        refresh.addActionListener(e -> refresh());
        filter.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        if (sp.getViewport() != null) sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        refresh();
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(230, 244, 255));
        t.setSelectionForeground(new Color(30, 41, 59));
        t.setBackground(Color.WHITE);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setBackground(new Color(248, 250, 252));
        h.setForeground(new Color(71, 85, 105));
        h.setFont(h.getFont().deriveFont(Font.BOLD, 12f));
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 36));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }

    private static void styleCombo(JComboBox<?> c) {
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(140, 30));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    }

    private static void styleActionButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(30, 41, 59));
    }

    private static final class ZebraRenderer extends DefaultTableCellRenderer {
        private static final Color ODD = new Color(255, 255, 255);
        private static final Color EVEN = new Color(249, 251, 253);

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground((row % 2 == 0) ? ODD : EVEN);
                c.setForeground(new Color(30, 41, 59));
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return c;
        }
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
