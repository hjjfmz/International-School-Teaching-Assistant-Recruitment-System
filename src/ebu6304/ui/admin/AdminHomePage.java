package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ebu6304.storage.DataService;
import ebu6304.ui.I18n;
import ebu6304.ui.IconFactory;

public final class AdminHomePage extends JPanel {
    public interface Nav {
        void go(String key);
    }

    private final StatCard taCard = new StatCard("TA Registered", "0", IconFactory.check(18, new Color(22, 119, 255)), new Color(22, 119, 255));
    private final StatCard userCard = new StatCard("Accounts", "0", IconFactory.user(18, new Color(245, 158, 11)), new Color(245, 158, 11));
    private final StatCard jobCard = new StatCard("Jobs", "0", IconFactory.hammer(18, new Color(16, 185, 129)), new Color(16, 185, 129));
    private final StatCard appCard = new StatCard("Applications", "0", IconFactory.envelope(18, new Color(168, 85, 247)), new Color(168, 85, 247));

    public AdminHomePage(DataService data, Nav nav) {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        JLabel title = new JLabel(I18n.t("admin.home.overview"));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 16f));
        title.setForeground(new Color(40, 40, 40));
        header.add(title, BorderLayout.WEST);
        add(header, BorderLayout.NORTH);

        JPanel content = new JPanel(new BorderLayout(10, 10));
        content.setOpaque(false);

        JPanel statsRow = new JPanel(new GridLayout(1, 4, 10, 10));
        statsRow.setOpaque(false);
        statsRow.add(taCard);
        statsRow.add(userCard);
        statsRow.add(jobCard);
        statsRow.add(appCard);
        content.add(statsRow, BorderLayout.NORTH);

        JPanel quickRow = new JPanel(new GridLayout(1, 3, 10, 10));
        quickRow.setOpaque(false);

        QuickNavCard users = new QuickNavCard(IconFactory.user(18, new Color(37, 99, 235)), I18n.t("nav.admin.users"), "Manage users & permissions", new Color(37, 99, 235));
        QuickNavCard export = new QuickNavCard(IconFactory.download(18, new Color(245, 158, 11)), I18n.t("nav.admin.export"), "Export data snapshots", new Color(245, 158, 11));
        QuickNavCard config = new QuickNavCard(IconFactory.gear(18, new Color(16, 185, 129)), I18n.t("nav.admin.config"), "System configuration", new Color(16, 185, 129));

        users.setOnClick(() -> { if (nav != null) nav.go(I18n.t("nav.admin.users")); });
        export.setOnClick(() -> { if (nav != null) nav.go(I18n.t("nav.admin.export")); });
        config.setOnClick(() -> { if (nav != null) nav.go(I18n.t("nav.admin.config")); });

        quickRow.add(users);
        quickRow.add(export);
        quickRow.add(config);
        content.add(quickRow, BorderLayout.CENTER);

        add(content, BorderLayout.CENTER);

        refresh(data);
    }

    public void refresh(DataService data) {
        int taCount = data.listApplicants().size();
        int userCount = data.listUsers().size();
        int jobCount = data.listJobs().size();
        int appCount = 0;
        for (ebu6304.model.Job j : data.listJobs()) {
            appCount += data.listApplicationsForJob(j.id()).size();
        }

        taCard.setValue(String.valueOf(taCount));
        userCard.setValue(String.valueOf(userCount));
        jobCard.setValue(String.valueOf(jobCount));
        appCard.setValue(String.valueOf(appCount));
    }

    private static final class StatCard extends RoundedPanel {
        private final JLabel title = new JLabel();
        private final JLabel value = new JLabel();
        private final JLabel icon = new JLabel();
        private final Color accent;

        private StatCard(String t, String v, javax.swing.ImageIcon ic, Color accent) {
            super(16);
            this.accent = accent;
            setBackground(tint(accent, 0.965f));
            setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

            setLayout(new GridBagLayout());

            icon.setIcon(ic);

            title.setText(t);
            title.setForeground(new Color(100, 116, 139));
            title.setFont(title.getFont().deriveFont(Font.PLAIN, 12f));

            value.setText(v);
            value.setForeground(new Color(30, 41, 59));
            value.setFont(value.getFont().deriveFont(Font.BOLD, 22f));

            GridBagConstraints gc = new GridBagConstraints();
            gc.gridx = 0;
            gc.gridy = 0;
            gc.anchor = GridBagConstraints.WEST;
            gc.insets = new Insets(0, 0, 8, 0);
            add(icon, gc);

            gc.gridy = 1;
            gc.insets = new Insets(0, 0, 2, 0);
            add(title, gc);

            gc.gridy = 2;
            gc.insets = new Insets(0, 0, 0, 0);
            add(value, gc);
        }

        private void setValue(String v) {
            value.setText(v == null ? "" : v);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(accent);
                g2.fillRoundRect(0, 0, 6, getHeight(), 16, 16);
            } finally {
                g2.dispose();
            }
        }
    }

    private static final class QuickNavCard extends RoundedPanel {
        private Runnable onClick;
        private boolean hover;
        private final Color accent;

        private QuickNavCard(javax.swing.ImageIcon ic, String title, String subtitle, Color accent) {
            super(18);
            this.accent = accent == null ? new Color(37, 99, 235) : accent;
            setBackground(tint(this.accent, 0.965f));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            setLayout(new BorderLayout(0, 10));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

            JPanel head = new JPanel(new BorderLayout(10, 0));
            head.setOpaque(false);

            JLabel icon = new JLabel(ic);

            JPanel texts = new JPanel(new GridLayout(2, 1, 0, 2));
            texts.setOpaque(false);
            JLabel t = new JLabel(title);
            t.setFont(t.getFont().deriveFont(Font.BOLD, 13f));
            t.setForeground(new Color(30, 41, 59));
            JLabel sub = new JLabel(subtitle);
            sub.setFont(sub.getFont().deriveFont(Font.PLAIN, 12f));
            sub.setForeground(new Color(100, 116, 139));
            texts.add(t);
            texts.add(sub);

            head.add(icon, BorderLayout.WEST);
            head.add(texts, BorderLayout.CENTER);
            add(head, BorderLayout.NORTH);

            JButton open = new JButton("Open \u2192");
            open.setFocusPainted(false);
            open.setBorderPainted(false);
            open.setContentAreaFilled(false);
            open.setForeground(new Color(22, 119, 255));
            open.setFont(open.getFont().deriveFont(Font.BOLD, 12f));
            open.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            add(open, BorderLayout.SOUTH);

            MouseAdapter hoverListener = new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    hover = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    hover = false;
                    repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    if (onClick != null) onClick.run();
                }
            };

            addMouseListener(hoverListener);
            open.addActionListener(e -> { if (onClick != null) onClick.run(); });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 24));
                g2.fillRoundRect(14, Math.max(12, getHeight() - 62), Math.max(0, getWidth() - 28), 42, 20, 20);
                g2.setColor(hover ? new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 190) : new Color(226, 232, 240, 190));
                g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), 18, 18);
            } finally {
                g2.dispose();
            }
        }

        private void setOnClick(Runnable r) {
            this.onClick = r;
        }
    }


    private static Color tint(Color c, float amount) {
        int r = (int) (255 - (255 - c.getRed()) * (1f - amount));
        int g = (int) (255 - (255 - c.getGreen()) * (1f - amount));
        int b = (int) (255 - (255 - c.getBlue()) * (1f - amount));
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }
    private static class RoundedPanel extends JPanel {
        private final int arc;

        private RoundedPanel(int arc) {
            super();
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 10));
                g2.fillRoundRect(2, 3, Math.max(0, getWidth() - 4), Math.max(0, getHeight() - 5), arc, arc);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);
                g2.setColor(new Color(226, 232, 240, 190));
                g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
