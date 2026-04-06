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
import javax.swing.ListSelectionModel;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import ebu6304.storage.AuthStore;
import ebu6304.storage.DataService;

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
                "Role", "Account", "Name", "Status"
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("User Management"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Role filter:"));
        left.add(roleFilter);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton enable = new JButton("Enable");
        JButton disable = new JButton("Disable");
        JButton addMo = new JButton("Add MO Account");
        JButton delete = new JButton("Delete");
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
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        model.setRowCount(0);
        String filter = String.valueOf(roleFilter.getSelectedItem());
        List<AuthStore.User> users = data.listUsers();
        for (AuthStore.User u : users) {
            if ("Admin".equalsIgnoreCase(u.role())) continue;
            if (!"All".equals(filter) && !filter.equalsIgnoreCase(u.role())) continue;
            model.addRow(new Object[] { u.role(), u.account(), u.name(), u.enabled() ? "Enabled" : "Disabled" });
        }
    }

    private void setUserEnabled(boolean enabled) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user");
            return;
        }
        String role = String.valueOf(model.getValueAt(r, 0));
        String account = String.valueOf(model.getValueAt(r, 1));
        if ("MO".equalsIgnoreCase(role) == false && "TA".equalsIgnoreCase(role) == false) {
            JOptionPane.showMessageDialog(this, "Only TA/MO users are supported");
            return;
        }
        boolean ok = data.setUserEnabled(actor, role, account, enabled);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Operation failed");
            return;
        }
        refresh();
    }

    private void addMoUser() {
        JTextField acc = new JTextField(16);
        JTextField pass = new JTextField(16);
        JTextField name = new JTextField(16);
        JPanel p = new JPanel(new java.awt.GridLayout(0, 1, 6, 6));
        p.add(new JLabel("Staff ID (Account)*"));
        p.add(acc);
        p.add(new JLabel("Password*"));
        p.add(pass);
        p.add(new JLabel("Name (optional)"));
        p.add(name);

        int res = JOptionPane.showConfirmDialog(this, p, "Add MO Account", JOptionPane.OK_CANCEL_OPTION);
        if (res != JOptionPane.OK_OPTION) return;

        String a = acc.getText().trim();
        String pw = pass.getText().trim();
        String nm = name.getText().trim();
        if (a.isEmpty() || pw.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Account and password are required");
            return;
        }

        data.upsertUser("MO", a, pw, nm);
        data.setUserEnabled("MO", a, true);
        refresh();
        JOptionPane.showMessageDialog(this, "Created");
    }

    private void delete() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select a user");
            return;
        }
        String role = String.valueOf(model.getValueAt(r, 0));
        String account = String.valueOf(model.getValueAt(r, 1));

        int ok = JOptionPane.showConfirmDialog(this, "This cannot be undone. Delete this account?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        boolean deleted = data.delete(actor, account);
        if (!deleted) {
            JOptionPane.showMessageDialog(this, "Delete failed");
            return;
        }
        refresh();
        JOptionPane.showMessageDialog(this, "Deleted");
    }
}
