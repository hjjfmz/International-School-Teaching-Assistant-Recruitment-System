package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
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
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import ebu6304.storage.AuthStore;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class AdminUserManagementPage extends JPanel {
    private final DataService data;
    private final String actor;

    private final JComboBox<String> roleFilter = new JComboBox<String>(new String[] { "All", "TA", "MO" });

    private final DefaultTableModel model;
    private final JTable table;

    public AdminUserManagementPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                I18n.t("admin.users.col.role"), I18n.t("admin.users.col.account"), I18n.t("admin.users.col.name"), I18n.t("admin.users.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        styleTable(table);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("admin.users.title")));
        top.setOpaque(false);

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);
        left.add(new JLabel(I18n.t("admin.users.rolefilter")));
        styleCombo(roleFilter);
        left.add(roleFilter);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.setOpaque(false);
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton enable = new JButton(I18n.t("common.enable"));
        JButton disable = new JButton(I18n.t("common.disable"));
        JButton addMo = new JButton(I18n.t("admin.users.addmo"));
        JButton delete = new JButton(I18n.t("common.delete"));

        styleActionButton(refresh);
        styleActionButton(enable);
        styleActionButton(disable);
        styleActionButton(addMo);
        styleDangerButton(delete);

        actions.add(refresh);
        actions.add(enable);
        actions.add(disable);
        actions.add(addMo);
        actions.add(delete);
        top.add(actions, BorderLayout.EAST);

        refresh.addActionListener(e -> refresh());
        roleFilter.addActionListener(e -> refresh());
        enable.addActionListener(e -> setUserEnabled(true));
        disable.addActionListener(e -> setUserEnabled(false));
        addMo.addActionListener(e -> addMoUser());
        delete.addActionListener(e -> delete());

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
        c.setPreferredSize(new Dimension(120, 30));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    }

    private static void styleActionButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(30, 41, 59));
    }

    private static void styleDangerButton(JButton b) {
        styleActionButton(b);
        b.setForeground(new Color(220, 38, 38));
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
        String filter = String.valueOf(roleFilter.getSelectedItem());
        List<AuthStore.User> users = data.listUsers();
        for (AuthStore.User u : users) {
            if ("Admin".equalsIgnoreCase(u.role())) continue;
            if (!"All".equals(filter) && !filter.equalsIgnoreCase(u.role())) continue;
            model.addRow(new Object[] { u.role(), u.account(), u.name(), u.enabled() ? I18n.t("admin.users.enabled") : I18n.t("admin.users.disabled") });
        }
    }

    private void setUserEnabled(boolean enabled) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.user"));
            return;
        }
        String role = String.valueOf(model.getValueAt(r, 0));
        String account = String.valueOf(model.getValueAt(r, 1));
        if ("MO".equalsIgnoreCase(role) == false && "TA".equalsIgnoreCase(role) == false) {
            JOptionPane.showMessageDialog(this, I18n.t("admin.users.only.tamo"));
            return;
        }
        boolean ok = data.setUserEnabled(actor, role, account, enabled);
        if (!ok) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.operation.failed"));
            return;
        }
        refresh();
    }

    private void addMoUser() {
        JTextField acc = new JTextField(16);
        JTextField pass = new JTextField(16);
        JTextField name = new JTextField(16);
        JPanel p = new JPanel(new java.awt.GridLayout(0, 1, 6, 6));
        p.add(new JLabel(I18n.t("admin.users.staffid")));
        p.add(acc);
        p.add(new JLabel(I18n.t("admin.users.password")));
        p.add(pass);
        p.add(new JLabel(I18n.t("admin.users.name")));
        p.add(name);

        int res = JOptionPane.showConfirmDialog(this, p, I18n.t("admin.users.addmo.title"), JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String a = acc.getText().trim();
        String pw = pass.getText().trim();
        String nm = name.getText().trim();
        if (a.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.account.password.required"));
            return;
        }

        data.upsertUser("MO", a, pw, nm);
        data.setUserEnabled("MO", a, true);
        refresh();
        JOptionPane.showMessageDialog(this, I18n.t("msg.created"));
    }

    private void delete() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.user"));
            return;
        }
        String role = String.valueOf(model.getValueAt(r, 0));
        String account = String.valueOf(model.getValueAt(r, 1));

        int ok = JOptionPane.showConfirmDialog(this, I18n.t("admin.users.confirm.delete"), I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean deleted = data.delete(actor, account);
        if (!deleted) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.delete.failed"));
            return;
        }
        refresh();
        JOptionPane.showMessageDialog(this, I18n.t("msg.deleted"));
    }
}
