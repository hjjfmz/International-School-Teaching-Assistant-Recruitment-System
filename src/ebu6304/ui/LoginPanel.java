package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.ImageIcon;
import javax.swing.SwingConstants;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

public final class LoginPanel extends JPanel {
    public interface LoginHandler {
        void onLogin(Role role, String account);
        void onForgotPassword();
    }

    private final DataService data;

    private static final Color BG = new Color(245, 247, 250);
    private static final Color CARD = Color.WHITE;
    private static final Color PRIMARY = new Color(22, 119, 255);

    public LoginPanel(DataService data, LoginHandler handler) {
        super(new GridBagLayout());
        this.data = data;
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);

        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);
        cardPanel.setOpaque(false);

        JPanel registerPanel = buildRegisterTab(() -> cards.show(cardPanel, "login"));
        JPanel loginPanel = buildLoginTab(handler, () -> cards.show(cardPanel, "register"));

        cardPanel.add(loginPanel, "login");
        cardPanel.add(registerPanel, "register");

        cards.show(cardPanel, "login");

        JPanel content = new JPanel(new BorderLayout());
        content.setOpaque(false);
        content.add(buildHeader(), BorderLayout.NORTH);
        content.add(cardPanel, BorderLayout.CENTER);

        RoundedPanel card = new RoundedPanel(18);
        card.setBackground(CARD);
        card.setBorder(BorderFactory.createEmptyBorder(18, 22, 18, 22));
        card.setLayout(new BorderLayout());
        card.add(content, BorderLayout.CENTER);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.anchor = GridBagConstraints.CENTER;
        c.fill = GridBagConstraints.NONE;
        add(card, c);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout(12, 12));
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 16, 0));
        header.setOpaque(false);

        JLabel logo = new JLabel("");
        logo.setHorizontalAlignment(SwingConstants.CENTER);
        logo.setPreferredSize(new Dimension(72, 72));

        try {
            File imgFile = new File(System.getProperty("user.dir"), "1.jpg");
            if (imgFile.isFile()) {
                BufferedImage img = ImageIO.read(imgFile);
                if (img != null) {
                    Image scaled = img.getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                    logo.setIcon(new ImageIcon(scaled));
                }
            }
        } catch (IOException ignored) {
        }

        JPanel titles = new JPanel(new BorderLayout());
        titles.setOpaque(false);
        JLabel title = new JLabel(I18n.t("app.title"));
        title.setFont(title.getFont().deriveFont(22f));
        JLabel subtitle = new JLabel(I18n.t("start.subtitle"));
        titles.add(title, BorderLayout.NORTH);
        titles.add(subtitle, BorderLayout.CENTER);

        header.add(logo, BorderLayout.WEST);
        header.add(titles, BorderLayout.CENTER);
        return header;
    }

    private JPanel buildLoginTab(LoginHandler handler, Runnable showRegister) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setOpaque(false);

        JTextField accountField = new JTextField(18);
        JPasswordField passField = new JPasswordField(18);

        JButton loginBtn = new JButton(I18n.t("login.button"));
        JButton forgotBtn = new JButton(I18n.t("login.forgot"));
        JButton registerBtn = new JButton(I18n.t("login.tab.register"));

        stylePrimaryButton(loginBtn);
        styleLinkButton(forgotBtn);
        styleLinkButton(registerBtn);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; p.add(new JLabel(I18n.t("login.account")), c);
        c.gridx = 1; c.gridy = 0; p.add(accountField, c);

        c.gridx = 0; c.gridy = 1; p.add(new JLabel(I18n.t("login.password")), c);
        c.gridx = 1; c.gridy = 1; p.add(passField, c);

        GridBagConstraints cBtn = new GridBagConstraints();
        cBtn.insets = new Insets(14, 6, 6, 6);
        cBtn.fill = GridBagConstraints.HORIZONTAL;
        cBtn.gridx = 0;
        cBtn.gridy = 2;
        cBtn.gridwidth = 2;
        loginBtn.setPreferredSize(new Dimension(0, 38));
        p.add(loginBtn, cBtn);

        JPanel links = new JPanel(new BorderLayout());
        links.setOpaque(false);
        links.add(forgotBtn, BorderLayout.WEST);
        links.add(registerBtn, BorderLayout.EAST);

        GridBagConstraints cLinks = new GridBagConstraints();
        cLinks.insets = new Insets(4, 6, 0, 6);
        cLinks.fill = GridBagConstraints.HORIZONTAL;
        cLinks.gridx = 0;
        cLinks.gridy = 3;
        cLinks.gridwidth = 2;
        p.add(links, cLinks);

        loginBtn.addActionListener(e -> {
            String account = accountField.getText().trim();
            String password = new String(passField.getPassword());

            if (account.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Account and password are required");
                return;
            }

            java.util.Optional<String> roleName = data.authenticateAndGetRole(account, password);
            if (!roleName.isPresent()) {
                JOptionPane.showMessageDialog(this, "Account or password incorrect");
                return;
            }

            Role role;
            if ("Admin".equalsIgnoreCase(roleName.get())) {
                role = Role.ADMIN;
            } else if ("MO".equalsIgnoreCase(roleName.get())) {
                role = Role.MO;
            } else {
                role = Role.TA;
            }

            if (handler != null) handler.onLogin(role, account);
        });

        forgotBtn.addActionListener(e -> {
            if (handler != null) handler.onForgotPassword();
        });

        registerBtn.addActionListener(e -> {
            if (showRegister != null) showRegister.run();
        });

        return p;
    }

    private static void stylePrimaryButton(JButton b) {
        b.setBackground(PRIMARY);
        b.setForeground(Color.WHITE);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setOpaque(true);
        b.setPreferredSize(new Dimension(120, 36));
    }

    private static void styleLinkButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(PRIMARY);
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
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private JPanel buildRegisterTab(Runnable showLogin) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        p.setOpaque(false);

        JTextField accountField = new JTextField(18);
        JTextField nameField = new JTextField(18);
        JTextField emailField = new JTextField(18);
        JPasswordField passField = new JPasswordField(18);
        JPasswordField pass2Field = new JPasswordField(18);
        JTextField skillsField = new JTextField(18);
        JTextField cvPathField = new JTextField(18);
        JCheckBox agreeBox = new JCheckBox(I18n.t("register.agree"));

        JButton browse = new JButton(I18n.t("register.browse"));
        JButton registerBtn = new JButton(I18n.t("register.button"));
        JButton backBtn = new JButton(I18n.t("login.tab.login"));

        stylePrimaryButton(registerBtn);
        styleLinkButton(backBtn);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; p.add(new JLabel(I18n.t("register.account")), c);
        c.gridx = 1; c.gridy = 0; p.add(accountField, c);

        c.gridx = 0; c.gridy = 1; p.add(new JLabel(I18n.t("register.name")), c);
        c.gridx = 1; c.gridy = 1; p.add(nameField, c);

        c.gridx = 0; c.gridy = 2; p.add(new JLabel(I18n.t("register.email")), c);
        c.gridx = 1; c.gridy = 2; p.add(emailField, c);

        c.gridx = 0; c.gridy = 3; p.add(new JLabel(I18n.t("register.password")), c);
        c.gridx = 1; c.gridy = 3; p.add(passField, c);

        c.gridx = 0; c.gridy = 4; p.add(new JLabel(I18n.t("register.password2")), c);
        c.gridx = 1; c.gridy = 4; p.add(pass2Field, c);

        c.gridx = 0; c.gridy = 5; p.add(new JLabel(I18n.t("register.skills")), c);
        c.gridx = 1; c.gridy = 5; p.add(skillsField, c);

        c.gridx = 0; c.gridy = 6; p.add(new JLabel(I18n.t("register.cv")), c);
        c.gridx = 1; c.gridy = 6; p.add(cvPathField, c);
        c.gridx = 2; c.gridy = 6; p.add(browse, c);

        c.gridx = 1; c.gridy = 7; p.add(agreeBox, c);
        JPanel actionBtns = new JPanel();
        actionBtns.setOpaque(false);
        actionBtns.add(backBtn);
        actionBtns.add(registerBtn);
        c.gridx = 1; c.gridy = 8; p.add(actionBtns, c);

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (f != null) cvPathField.setText(f.getAbsolutePath());
            }
        });

        backBtn.addActionListener(e -> {
            if (showLogin != null) showLogin.run();
        });

        registerBtn.addActionListener(e -> {
            String account = accountField.getText().trim();
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String pass = new String(passField.getPassword());
            String pass2 = new String(pass2Field.getPassword());
            String skills = skillsField.getText().trim();
            String cvPath = cvPathField.getText().trim();

            if (account.isEmpty() || name.isEmpty() || email.isEmpty() || pass.isEmpty() || pass2.isEmpty() || cvPath.isEmpty()) {
                JOptionPane.showMessageDialog(this, "All fields marked with * are required");
                return;
            }
            if (!pass.equals(pass2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }

            int minLen = data.getConfig().passwordMinLength();
            if (pass.length() < minLen) {
                JOptionPane.showMessageDialog(this, "Password must be at least " + minLen + " characters");
                return;
            }
            if (!agreeBox.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please accept the registration terms");
                return;
            }
            if (data.getApplicant(account).isPresent()) {
                JOptionPane.showMessageDialog(this, "Account already exists");
                return;
            }
            if (!isSupportedCv(cvPath, data.getConfig().cvFormats())) {
                JOptionPane.showMessageDialog(this, "Unsupported CV format. Allowed: " + data.getConfig().cvFormats());
                return;
            }

            String storedCvPath;
            try {
                storedCvPath = data.storeCv(account, cvPath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Unable to save CV into project data folder");
                return;
            }

            Applicant a = data.upsertApplicantByAccount(account, name, email, skills, storedCvPath);
            data.upsertUser(Role.TA.authRole(), a.id(), pass, a.name());
            JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
            if (showLogin != null) showLogin.run();
        });

        return p;
    }

    private static boolean isSupportedCv(String path, String formatsCsv) {
        if (path == null) return false;
        String p = path.toLowerCase();
        int dot = p.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = p.substring(dot + 1);
        if (formatsCsv == null || formatsCsv.trim().isEmpty()) {
            return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx");
        }
        String[] parts = formatsCsv.toLowerCase().split(",");
        for (String s : parts) {
            String f = s.trim();
            if (f.isEmpty()) continue;
            if (ext.equals(f)) return true;
        }
        return false;
    }
}
