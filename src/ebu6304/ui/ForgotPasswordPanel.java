package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

public final class ForgotPasswordPanel extends JPanel {
    public interface BackHandler {
        void onBack();
    }

    private final DataService data;
    private static final Color PRIMARY = UiTheme.PRIMARY;
    private static final Color LINK_COLOR = UiTheme.PRIMARY;

    public ForgotPasswordPanel(DataService data, BackHandler back) {
        super(new BorderLayout());
        this.data = data;
        setBackground(UiTheme.APP_BG);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel tabTitle = new JLabel(I18n.t("forgot.title"));
        tabTitle.setFont(tabTitle.getFont().deriveFont(java.awt.Font.BOLD, 18f));
        tabTitle.setForeground(new Color(51, 51, 51));
        tabTitle.setAlignmentX(0f);
        p.add(tabTitle);

        JPanel underline = new JPanel();
        underline.setBackground(PRIMARY);
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(0f);
        p.add(Box.createVerticalStrut(6));
        p.add(underline);
        p.add(Box.createVerticalStrut(20));

        JComboBox<Role> roleBox = new JComboBox<Role>(Role.values());
        styleInput(roleBox);
        JLabel roleLabel = new JLabel(I18n.t("forgot.role"));
        roleLabel.setFont(roleLabel.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        roleLabel.setForeground(new Color(102, 102, 102));
        roleLabel.setAlignmentX(0f);
        p.add(roleLabel);
        p.add(Box.createVerticalStrut(4));
        JPanel roleRow = new JPanel(new BorderLayout());
        roleRow.setOpaque(false);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        roleBox.setPreferredSize(new Dimension(0, 36));
        roleRow.add(roleBox, BorderLayout.CENTER);
        roleRow.setAlignmentX(0f);
        p.add(roleRow);
        p.add(Box.createVerticalStrut(10));

        JTextField accountField = new JTextField(20);
        JTextField verifyField = new JTextField(20);
        JPasswordField newPassField = new JPasswordField(20);
        JPasswordField newPass2Field = new JPasswordField(20);
        for (JTextField tf : new JTextField[]{accountField, verifyField, newPassField, newPass2Field}) {
            styleInput(tf);
        }

        addField(p, accountField, I18n.t("login.account"));
        addField(p, verifyField, I18n.t("forgot.verify"));
        addField(p, newPassField, I18n.t("forgot.newpass"));
        addField(p, newPass2Field, I18n.t("forgot.newpass2"));

        JButton resetBtn = new JButton(I18n.t("forgot.button"));
        stylePrimaryButton(resetBtn);
        resetBtn.setAlignmentX(0f);
        resetBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(resetBtn);
        p.add(Box.createVerticalStrut(14));

        JButton backBtn = new JButton(I18n.t("common.back"));
        styleLinkButton(backBtn);
        backBtn.setAlignmentX(0f);
        p.add(backBtn);

        RoundedPanel sideCard = new RoundedPanel(24);
        sideCard.setBackground(new Color(255, 255, 255, 240));
        sideCard.setLayout(new BorderLayout());
        sideCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sideCard.setPreferredSize(new Dimension(420, 420));
        sideCard.setMinimumSize(new Dimension(400, 380));
        sideCard.add(p, BorderLayout.CENTER);

        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 1;
        gc.gridy = 0;
        gc.weightx = 0;
        gc.weighty = 1;
        gc.anchor = GridBagConstraints.EAST;
        gc.fill = GridBagConstraints.NONE;
        gc.insets = new java.awt.Insets(20, 0, 20, 60);

        GridBagConstraints spacer = new GridBagConstraints();
        spacer.gridx = 0;
        spacer.gridy = 0;
        spacer.weightx = 1;
        spacer.fill = GridBagConstraints.HORIZONTAL;
        centerWrapper.add(Box.createGlue(), spacer);

        centerWrapper.add(sideCard, gc);
        add(centerWrapper, BorderLayout.CENTER);

        resetBtn.addActionListener(e -> {
            Role role = (Role) roleBox.getSelectedItem();
            String account = accountField.getText().trim();
            String verify = verifyField.getText().trim();
            String np = new String(newPassField.getPassword());
            String np2 = new String(newPass2Field.getPassword());

            if (account.isEmpty() || np.isEmpty() || np2.isEmpty()) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.account.required"));
                return;
            }
            if (!np.equals(np2)) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.password.mismatch"));
                return;
            }
            int minLen = data.getConfig().passwordMinLength();
            if (np.length() < minLen) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.password.tooshort", minLen));
                return;
            }
            if (role == Role.TA) {
                Applicant a = data.getApplicant(account).orElse(null);
                if (a == null) {
                    JOptionPane.showMessageDialog(this, I18n.t("msg.account.notfound"));
                    return;
                }
                if (verify.isEmpty() || !verify.equalsIgnoreCase(a.email())) {
                    JOptionPane.showMessageDialog(this, I18n.t("msg.verify.mismatch"));
                    return;
                }
            }

            boolean ok = data.resetPassword(role.authRole(), account, np);
            if (!ok) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.role.mismatch"));
                return;
            }

            JOptionPane.showMessageDialog(this, I18n.t("msg.reset.success"));
            if (back != null) back.onBack();
        });

        backBtn.addActionListener(e -> {
            if (back != null) back.onBack();
        });
    }

    private static void addField(JPanel p, JTextField tf, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(java.awt.Font.PLAIN, 12f));
        lbl.setForeground(new Color(102, 102, 102));
        lbl.setAlignmentX(0f);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setPreferredSize(new Dimension(0, 36));
        row.add(tf, BorderLayout.CENTER);
        row.setAlignmentX(0f);
        p.add(row);
        p.add(Box.createVerticalStrut(10));
    }

    private static void styleInput(JTextField tf) {
        UiTheme.styleTextField(tf);
        tf.setFont(tf.getFont().deriveFont(java.awt.Font.PLAIN, 14f));
    }

    private static void styleInput(JComboBox<?> cb) {
        UiTheme.styleCombo(cb);
        cb.setFont(cb.getFont().deriveFont(java.awt.Font.PLAIN, 14f));
    }

    private static void stylePrimaryButton(JButton b) {
        UiTheme.stylePrimaryButton(b);
        b.setFont(b.getFont().deriveFont(java.awt.Font.BOLD, 15f));
        b.setPreferredSize(new Dimension(0, 42));
    }

    private static void styleLinkButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(LINK_COLOR);
        b.setFont(b.getFont().deriveFont(java.awt.Font.BOLD, 12f));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    }

    private static final class RoundedPanel extends JPanel {
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
                g2.setColor(new Color(15, 23, 42, 26));
                g2.fillRoundRect(5, 7, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 12), arc, arc);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);
                g2.setColor(new Color(148, 163, 184, 70));
                g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }
}
