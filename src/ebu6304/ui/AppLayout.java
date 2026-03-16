package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JList;
import javax.swing.ListSelectionModel;

public final class AppLayout extends JPanel {
    public interface LogoutHandler {
        void onLogout();
    }

    public interface NavHandler {
        void onNavigate(String key);
    }

    private final JLabel titleLabel = new JLabel(I18n.t("app.title"));
    private final JLabel roleLabel = new JLabel(" ");
    private final JLabel accountLabel = new JLabel(" ");
    private final JButton logoutBtn = new JButton(I18n.t("common.logout"));

    private final StatusBar statusBar = new StatusBar();

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel content = new JPanel(contentLayout);

    private final JList<String> navList;

    public AppLayout(String[] navItems, LogoutHandler logout, NavHandler nav) {
        super(new BorderLayout());

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        JPanel left = new JPanel(new BorderLayout());
        left.add(titleLabel, BorderLayout.WEST);
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.add(roleLabel);
        right.add(accountLabel);
        right.add(logoutBtn);
        top.add(right, BorderLayout.EAST);

        logoutBtn.addActionListener(e -> {
            if (logout != null) logout.onLogout();
        });

        navList = new JList<String>(navItems == null ? new String[0] : navItems);
        navList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        navList.setFixedCellWidth(220);
        navList.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        navList.addListSelectionListener(e -> {
            if (e.getValueIsAdjusting()) return;
            String v = navList.getSelectedValue();
            if (v == null) return;
            if (nav != null) nav.onNavigate(v);
        });

        JScrollPane navScroll = new JScrollPane(navList);
        navScroll.setPreferredSize(new Dimension(240, 0));

        add(top, BorderLayout.NORTH);
        add(navScroll, BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        statusBar.setLeftText(I18n.t("status.ready"));
    }

    public void setUser(Role role, String account) {
        roleLabel.setText("[" + I18n.t(role.displayKey()) + "]");
        accountLabel.setText(account == null ? "" : account);
    }

    public StatusBar statusBar() {
        return statusBar;
    }

    public void setNavSelectedIndex(int idx) {
        if (idx < 0 || idx >= navList.getModel().getSize()) return;
        navList.setSelectedIndex(idx);
    }

    public void addContent(String key, JPanel panel) {
        if (key == null || panel == null) return;
        content.add(panel, key);
    }

    public void showContent(String key) {
        if (key == null) return;
        contentLayout.show(content, key);
    }
}
