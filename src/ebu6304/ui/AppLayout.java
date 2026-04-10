package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.util.function.Supplier;

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
    private final BadgeIconButton notifBtn = new BadgeIconButton(IconFactory.bell(32, new Color(90, 98, 110)));
    private final BadgeIconButton settingsBtn = new BadgeIconButton(IconFactory.gear(32, new Color(90, 98, 110)));
    private final JButton avatarBtn = new JButton("");

    private int unreadNotifications = 0;

    private Role currentRole;
    private String currentAccount;

    private Runnable onNotificationsOpened;
    private Supplier<String> notificationsTextSupplier;
    private Runnable onLanguageChange;

    private final StatusBar statusBar = new StatusBar();

    private final CardLayout contentLayout = new CardLayout();
    private final JPanel content = new JPanel(contentLayout);

    private final List<NavItemButton> navButtons = new ArrayList<NavItemButton>();
    private final GradientPanel navPanel;
    private final RoundedPanel contentCard;
    private final JLabel navTitle;

    private Color theme1 = new Color(21, 101, 192);
    private Color theme2 = new Color(13, 71, 161);
    private Color accent = new Color(22, 119, 255);
    private static final Color APP_BG = new Color(245, 247, 250);
    private static final Color CARD_BG = Color.WHITE;

    public AppLayout(Role role, String[] navItems, LogoutHandler logout, NavHandler nav, Runnable onLanguageChange) {
        super(new BorderLayout());

        this.onLanguageChange = onLanguageChange;
        applyTheme(role);
        setBackground(APP_BG);

        JPanel top = new JPanel(new BorderLayout(12, 12));
        top.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(232, 236, 242)),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)));
        top.setBackground(CARD_BG);

        JPanel left = new JPanel(new BorderLayout(8, 8));
        left.setOpaque(false);
        JLabel appIcon = new JLabel("");
        try {
            java.io.File f = new java.io.File(ebu6304.App.projectRoot(), "1.jpg");
            if (f.isFile()) {
                java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(f);
                if (img != null) {
                    java.awt.Image scaled = img.getScaledInstance(22, 22, java.awt.Image.SCALE_SMOOTH);
                    appIcon.setIcon(new ImageIcon(scaled));
                }
            }
        } catch (Exception ignored) {
        }
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));
        left.add(appIcon, BorderLayout.WEST);
        left.add(titleLabel, BorderLayout.CENTER);
        top.add(left, BorderLayout.WEST);

        JPanel right = new JPanel();
        right.setOpaque(false);
        roleLabel.setForeground(new Color(110, 118, 130));
        accountLabel.setForeground(new Color(110, 118, 130));
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        accountLabel.setFont(accountLabel.getFont().deriveFont(Font.PLAIN, 12f));
        styleTopIconButton(notifBtn);
        styleTopIconButton(settingsBtn);
        buildAvatarButton();

        right.add(roleLabel);
        right.add(Box.createHorizontalStrut(6));
        right.add(accountLabel);
        right.add(Box.createHorizontalStrut(10));
        right.add(notifBtn);
        right.add(settingsBtn);
        right.add(avatarBtn);
        top.add(right, BorderLayout.EAST);

        logoutBtn.addActionListener(e -> {
            if (logout != null) logout.onLogout();
        });

        notifBtn.addActionListener(e -> {
            openNotifications();
        });
        settingsBtn.addActionListener(e -> {
            openSettings();
        });

        notifBtn.setBadgeVisible(false);
        settingsBtn.setBadgeVisible(false);

        navPanel = new GradientPanel(theme1, theme2);
        navPanel.setLayout(new BoxLayout(navPanel, BoxLayout.Y_AXIS));
        navPanel.setBorder(BorderFactory.createEmptyBorder(18, 14, 18, 14));
        navPanel.setPreferredSize(new Dimension(260, 0));

        navTitle = new JLabel(role == null ? "" : I18n.t(role.displayKey()), SwingConstants.LEFT);
        navTitle.setForeground(Color.WHITE);
        navTitle.setFont(navTitle.getFont().deriveFont(Font.BOLD, 18f));
        navTitle.setBorder(BorderFactory.createEmptyBorder(0, 10, 12, 10));
        navTitle.setAlignmentX(0f);
        navPanel.add(navTitle);

        if (navItems != null) {
            for (int i = 0; i < navItems.length; i++) {
                String key = navItems[i];
                if (key == null) continue;
                ImageIcon navIcon = iconFor(key, role);
                NavItemButton b = new NavItemButton(key, navIcon);
                b.setForeground(Color.WHITE);
                b.setFocusPainted(false);
                b.setBorderPainted(false);
                b.setContentAreaFilled(false);
                b.setHorizontalAlignment(SwingConstants.LEFT);
                b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                b.setMargin(new Insets(12, 14, 12, 14));
                b.setAlignmentX(0f);
                b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        b.setHover(true);
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        b.setHover(false);
                    }
                });
                b.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        selectNav(b);
                        if (nav != null) nav.onNavigate(b.key);
                    }
                });
                navButtons.add(b);
                navPanel.add(b);
                navPanel.add(Box.createVerticalStrut(8));
            }
        }
        navPanel.add(Box.createVerticalGlue());

        JPanel centerHolder = new JPanel(new BorderLayout());
        centerHolder.setBackground(APP_BG);
        centerHolder.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        content.setOpaque(false);
        contentCard = new RoundedPanel(20, true);
        contentCard.setBackground(CARD_BG);
        contentCard.setLayout(new BorderLayout());
        contentCard.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        contentCard.add(content, BorderLayout.CENTER);
        centerHolder.add(contentCard, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);
        add(navPanel, BorderLayout.WEST);
        add(centerHolder, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);

        statusBar.setLeftText(I18n.t("status.ready"));
    }

    private void applyTheme(Role role) {
        if (role == Role.ADMIN) {
            theme1 = new Color(255, 126, 0);
            theme2 = new Color(255, 87, 34);
            accent = new Color(255, 110, 64);
        } else if (role == Role.MO) {
            theme1 = new Color(0, 191, 165);
            theme2 = new Color(0, 150, 136);
            accent = new Color(0, 191, 165);
        } else {
            theme1 = new Color(21, 101, 192);
            theme2 = new Color(13, 71, 161);
            accent = new Color(22, 119, 255);
        }
    }

    public void setOnNotificationsOpened(Runnable r) {
        this.onNotificationsOpened = r;
    }

    public void setNotificationsTextSupplier(Supplier<String> s) {
        this.notificationsTextSupplier = s;
    }

    private void selectNav(NavItemButton selected) {
        for (NavItemButton b : navButtons) {
            b.setSelected(b == selected);
        }
        navPanel.repaint();
    }

    private void selectNavByKey(String key) {
        if (key == null) return;
        for (NavItemButton b : navButtons) {
            if (key.equals(b.key)) {
                selectNav(b);
                return;
            }
        }
    }

    private static void styleTopIconButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setForeground(new Color(90, 98, 110));
        b.setFont(b.getFont().deriveFont(Font.PLAIN, 16f));
    }

    private void buildAvatarButton() {
        avatarBtn.setPreferredSize(new Dimension(34, 34));
        avatarBtn.setFocusPainted(false);
        avatarBtn.setBorderPainted(false);
        avatarBtn.setContentAreaFilled(false);
        avatarBtn.setOpaque(false);
        avatarBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        avatarBtn.setIcon(loadAvatarIcon());

        JPopupMenu menu = new JPopupMenu();
        JMenuItem profileItem = new JMenuItem(I18n.t("layout.profile"));
        profileItem.addActionListener(e -> openProfile());
        JMenuItem logoutItem = new JMenuItem(I18n.t("common.logout"));
        logoutItem.addActionListener(e -> logoutBtn.doClick());
        menu.add(profileItem);
        menu.addSeparator();
        menu.add(logoutItem);

        avatarBtn.addActionListener(e -> {
            menu.show(avatarBtn, 0, avatarBtn.getHeight());
        });
    }

    private static ImageIcon loadAvatarIcon() {
        try {
            java.io.File f = new java.io.File(ebu6304.App.projectRoot(), "1.jpg");
            if (!f.isFile()) return null;
            BufferedImage img = javax.imageio.ImageIO.read(f);
            if (img == null) return null;

            BufferedImage out = new BufferedImage(28, 28, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g2 = out.createGraphics();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setClip(new Ellipse2D.Double(0, 0, 28, 28));
                java.awt.Image scaled = img.getScaledInstance(28, 28, java.awt.Image.SCALE_SMOOTH);
                g2.drawImage(scaled, 0, 0, null);
            } finally {
                g2.dispose();
            }
            return new ImageIcon(out);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static ImageIcon iconFor(String key, Role role) {
        Color fg = Color.WHITE;
        int sz = 32;
        String k = key == null ? "" : key.toLowerCase();
        if (k.contains("home")) return IconFactory.home(sz, fg);
        if (k.contains("profile") || k.contains("user")) return IconFactory.user(sz, fg);
        if (k.contains("resume")) return IconFactory.document(sz, fg);
        if (k.contains("job")) return IconFactory.hammer(sz, fg);
        if (k.contains("application")) return IconFactory.envelope(sz, fg);
        if (k.contains("result")) return IconFactory.check(sz, fg);
        if (k.contains("config") || k.contains("system")) return IconFactory.gear(sz, fg);
        if (k.contains("export")) return IconFactory.download(sz, fg);
        if (k.contains("log")) return IconFactory.menu(sz, fg);
        if (role == Role.ADMIN) return IconFactory.shield(sz, fg);
        return IconFactory.bullet(sz, fg);
    }

    private static ImageIcon iconForColor(String key, Role role, Color fg) {
        int sz = 32;
        String k = key == null ? "" : key.toLowerCase();
        if (k.contains("home")) return IconFactory.home(sz, fg);
        if (k.contains("profile") || k.contains("user")) return IconFactory.user(sz, fg);
        if (k.contains("resume")) return IconFactory.document(sz, fg);
        if (k.contains("job")) return IconFactory.hammer(sz, fg);
        if (k.contains("application")) return IconFactory.envelope(sz, fg);
        if (k.contains("result")) return IconFactory.check(sz, fg);
        if (k.contains("config") || k.contains("system")) return IconFactory.gear(sz, fg);
        if (k.contains("export")) return IconFactory.download(sz, fg);
        if (k.contains("log")) return IconFactory.menu(sz, fg);
        if (role == Role.ADMIN) return IconFactory.shield(sz, fg);
        return IconFactory.bullet(sz, fg);
    }

    public void setUser(Role role, String account) {
        currentRole = role;
        currentAccount = account;
        roleLabel.setText("[" + I18n.t(role.displayKey()) + "]");
        accountLabel.setText(account == null ? "" : account);
        navTitle.setText(role == null ? "" : I18n.t(role.displayKey()));
    }

    public StatusBar statusBar() {
        return statusBar;
    }

    public void setNavSelectedIndex(int idx) {
        if (idx < 0 || idx >= navButtons.size()) return;
        selectNav(navButtons.get(idx));
    }

    public void addContent(String key, JPanel panel) {
        if (key == null || panel == null) return;
        content.add(panel, key);
    }

    public void showContent(String key) {
        if (key == null) return;
        selectNavByKey(key);
        contentLayout.show(content, key);
    }

    public void setUnreadNotifications(int unreadCount) {
        unreadNotifications = Math.max(0, unreadCount);
        notifBtn.setBadgeVisible(unreadNotifications > 0);
        if (unreadNotifications > 0) {
            notifBtn.setToolTipText(I18n.t("layout.notifications.unread", unreadNotifications));
        } else {
            notifBtn.setToolTipText(I18n.t("layout.notifications"));
        }
    }

    private void openProfile() {
        statusBar.setLeftText(I18n.t("layout.profile"));
        String r = currentRole == null ? "" : I18n.t(currentRole.displayKey());
        String a = currentAccount == null ? "" : currentAccount;
        JOptionPane.showMessageDialog(this,
                I18n.t("layout.profile") + "\n\nRole: " + r + "\nAccount: " + a,
                I18n.t("layout.profile"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private void openSettings() {
        statusBar.setLeftText(I18n.t("layout.settings"));
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JPanel row = new JPanel(new BorderLayout(10, 10));
        row.add(new JLabel(I18n.t("layout.language")), BorderLayout.WEST);
        JComboBox<I18n.Lang> langBox = new JComboBox<I18n.Lang>(I18n.Lang.values());
        langBox.setSelectedItem(I18n.lang());
        row.add(langBox, BorderLayout.CENTER);

        p.add(row, BorderLayout.NORTH);

        int res = JOptionPane.showConfirmDialog(this, p, I18n.t("layout.settings"), JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);
        if (res != JOptionPane.OK_OPTION) return;

        I18n.Lang selected = (I18n.Lang) langBox.getSelectedItem();
        if (selected != null && selected != I18n.lang()) {
            I18n.setLang(selected);
            if (onLanguageChange != null) {
                onLanguageChange.run();
            } else {
                refreshI18nTexts();
            }
        }
    }

    private void refreshI18nTexts() {
        titleLabel.setText(I18n.t("app.title"));
        logoutBtn.setText(I18n.t("common.logout"));
        if (currentRole != null) {
            roleLabel.setText("[" + I18n.t(currentRole.displayKey()) + "]");
            navTitle.setText(I18n.t(currentRole.displayKey()));
        }
        statusBar.setLeftText(I18n.t("status.ready"));
        revalidate();
        repaint();
    }

    private void openNotifications() {
        statusBar.setLeftText(I18n.t("layout.notifications"));

        String text = "";
        if (notificationsTextSupplier != null) {
            try {
                String v = notificationsTextSupplier.get();
                if (v != null) text = v;
            } catch (Exception ignored) {
            }
        }

        if (text.trim().isEmpty()) {
            text = unreadNotifications > 0
                    ? I18n.t("layout.notifications.hasunread", unreadNotifications)
                    : I18n.t("layout.notifications.none");
        }

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setCaretPosition(area.getDocument().getLength());

        JScrollPane sp = new JScrollPane(area);
        sp.setPreferredSize(new Dimension(760, 420));

        JOptionPane.showMessageDialog(this, sp, I18n.t("layout.notifications"), JOptionPane.INFORMATION_MESSAGE);
        if (unreadNotifications > 0) setUnreadNotifications(0);
        if (onNotificationsOpened != null) onNotificationsOpened.run();
    }

    private final class NavItemButton extends JButton {
        private final String key;
        private final ImageIcon icon;
        private boolean selected;
        private boolean hover;

        private NavItemButton(String key, ImageIcon icon) {
            super(key);
            this.key = key;
            this.icon = icon;
            setFont(getFont().deriveFont(14f));
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            repaint();
        }

        public void setHover(boolean hover) {
            this.hover = hover;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            if (hover && !selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                } finally {
                    g2.dispose();
                }
            }
            if (selected) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(new Color(255, 255, 255, 235));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    g2.setColor(accent);
                    g2.fillRoundRect(6, 6, 4, getHeight() - 12, 6, 6);
                } finally {
                    g2.dispose();
                }
                setForeground(new Color(accent.getRed(), accent.getGreen(), accent.getBlue()));
            } else {
                setForeground(Color.WHITE);
            }

            if (icon != null) {
                ImageIcon toDraw = icon;
                if (selected) {
                    toDraw = iconForColor(key, currentRole,
                            new Color(accent.getRed(), accent.getGreen(), accent.getBlue()));
                }
                int ix = 14;
                int iy = (getHeight() - toDraw.getIconHeight()) / 2;
                toDraw.paintIcon(this, g, ix, iy);
                Insets m = getMargin();
                if (m == null || m.left < 34) {
                    setMargin(new Insets(12, 40, 12, 14));
                }
            }
            super.paintComponent(g);
        }
    }

    private static final class GradientPanel extends JPanel {
        private final Color c1;
        private final Color c2;

        private GradientPanel(Color c1, Color c2) {
            super();
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 18));
                g2.fillRoundRect(4, 6, getWidth() - 6, getHeight() - 6, 28, 28);
                java.awt.GradientPaint gp = new java.awt.GradientPaint(0, 0, c1, 0, getHeight(), c2);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth() - 2, getHeight() - 2, 28, 28);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private static final class BadgeIconButton extends JButton {
        private boolean badgeVisible;

        private BadgeIconButton(ImageIcon icon) {
            super();
            setIcon(icon);
        }

        public void setBadgeVisible(boolean visible) {
            this.badgeVisible = visible;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (!badgeVisible) return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int d = 8;
                int x = getWidth() - d - 4;
                int y = 4;
                g2.setColor(new Color(255, 59, 48));
                g2.fillOval(x, y, d, d);
                g2.setColor(Color.WHITE);
                g2.drawOval(x, y, d, d);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class RoundedPanel extends JPanel {
        private final int arc;
        private final boolean shadow;

        private RoundedPanel(int arc, boolean shadow) {
            super();
            this.arc = arc;
            this.shadow = shadow;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (shadow) {
                    g2.setColor(new Color(0, 0, 0, 16));
                    g2.fillRoundRect(4, 6, getWidth() - 8, getHeight() - 8, arc, arc);
                }
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
