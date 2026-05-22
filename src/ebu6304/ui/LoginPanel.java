package ebu6304.ui;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;
import ebu6304.storage.OperationLog;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

public final class LoginPanel extends JPanel {
    public interface LoginHandler {
        void onLogin(Role role, String account);
    }

    private final DataService data;

    private PlaceholderTextField accountField;
    private PlaceholderPasswordField passField;

    private static final Color PRIMARY = UiTheme.PRIMARY;
    private static final Color HEADER_BG = new Color(255, 255, 255, 232);
    private static final Color CARD_BG = new Color(255, 255, 255, 246);
    private static final Color FOOTER_BG = new Color(15, 23, 42, 118);
    private static final Color LINK_COLOR = UiTheme.PRIMARY;

    private BufferedImage bgImage;

    public LoginPanel(DataService data, LoginHandler handler) {
        super(new BorderLayout());
        this.data = data;
        setBackground(UiTheme.APP_BG);

        loadBackground();

        add(buildHeaderBar(), BorderLayout.NORTH);

        JPanel centerWrapper = new JPanel(new GridBagLayout());
        centerWrapper.setOpaque(false);

        CardLayout cards = new CardLayout();
        JPanel cardPanel = new JPanel(cards);
        cardPanel.setOpaque(false);

        JPanel loginTab = buildLoginTab(handler,
                () -> cards.show(cardPanel, "register"),
                () -> cards.show(cardPanel, "forgot"));
        JPanel registerTab = buildRegisterTab(() -> cards.show(cardPanel, "login"));
        JPanel forgotTab = buildForgotTab(() -> cards.show(cardPanel, "login"));
        cardPanel.add(loginTab, "login");
        cardPanel.add(registerTab, "register");
        cardPanel.add(forgotTab, "forgot");
        cards.show(cardPanel, "login");

        RoundedPanel sideCard = new RoundedPanel(24);
        sideCard.setBackground(CARD_BG);
        sideCard.setLayout(new BorderLayout());
        sideCard.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sideCard.setPreferredSize(new Dimension(430, 540));
        sideCard.setMinimumSize(new Dimension(390, 320));
        sideCard.add(cardPanel, BorderLayout.CENTER);

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

        add(buildFooterBar(), BorderLayout.SOUTH);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        try {
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            if (bgImage != null) {
                g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                int pw = getWidth(), ph = getHeight();
                int iw = bgImage.getWidth(), ih = bgImage.getHeight();
                double scale = Math.max((double) pw / iw, (double) ph / ih);
                int sw = (int) (iw * scale), sh = (int) (ih * scale);
                int x = (pw - sw) / 2, y = (ph - sh) / 2;
                g2.drawImage(bgImage, x, y, sw, sh, null);
                g2.setColor(new Color(15, 23, 42, 58));
                g2.fillRect(0, 0, getWidth(), getHeight());
            } else {
                g2.setPaint(new java.awt.GradientPaint(0, 0, new Color(248, 251, 255), getWidth(), getHeight(), new Color(226, 242, 255)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(37, 99, 235, 24));
                g2.fillRoundRect(-90, 96, 520, 120, 34, 34);
                g2.setColor(new Color(16, 185, 129, 22));
                g2.fillRoundRect(90, 260, 520, 92, 30, 30);
                g2.setColor(new Color(245, 158, 11, 22));
                g2.fillRoundRect(0, 430, 430, 78, 28, 28);
            }
        } finally {
            g2.dispose();
        }
    }

    private void loadBackground() {
        String base = ebu6304.App.projectRoot().getAbsolutePath();
        String[] candidates = { "images/login-bg.jpg", "images/login-bg.png", "images/bg.png" };
        for (String c : candidates) {
            try {
                File f = new File(base, c);
                if (f.isFile()) {
                    bgImage = ImageIO.read(f);
                    if (bgImage != null) return;
                }
            } catch (IOException ignored) {}
        }
    }

    private JPanel buildHeaderBar() {
        JPanel header = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(HEADER_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        header.setOpaque(false);
        header.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        header.setPreferredSize(new Dimension(0, 56));

        JPanel logoRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        logoRow.setOpaque(false);

        JLabel logoLabel = new JLabel();
        try {
            File logoFile = new File(ebu6304.App.projectRoot(), "images/logo-full.png");
            if (logoFile.isFile()) {
                BufferedImage img = ImageIO.read(logoFile);
                if (img != null) {
                    int h = 32;
                    int w = (int) ((double) img.getWidth() / img.getHeight() * h);
                    Image scaled = img.getScaledInstance(w, h, Image.SCALE_SMOOTH);
                    logoLabel.setIcon(new ImageIcon(scaled));
                }
            }
        } catch (IOException ignored) {}

        JLabel titleLabel = new JLabel(I18n.t("app.title"));
        titleLabel.setForeground(UiTheme.TEXT);
        titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 16f));

        logoRow.add(logoLabel);
        logoRow.add(titleLabel);
        header.add(logoRow, BorderLayout.WEST);

        return header;
    }

    private JPanel buildFooterBar() {
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setColor(FOOTER_BG);
                    g2.fillRect(0, 0, getWidth(), getHeight());
                } finally {
                    g2.dispose();
                }
                super.paintComponent(g);
            }
        };
        footer.setOpaque(false);
        footer.setBorder(BorderFactory.createEmptyBorder(8, 0, 8, 0));
        JLabel copy = new JLabel(I18n.t("login.footer"));
        copy.setForeground(new Color(226, 232, 240));
        copy.setFont(copy.getFont().deriveFont(Font.PLAIN, 11f));
        footer.add(copy);
        return footer;
    }

    /** 登出时调用，清除输入框中的账号和密码。 */
    public void clearCredentials() {
        if (accountField != null) accountField.setText("");
        if (passField != null) passField.setText("");
    }

    /* ── Login tab ───────────────────────────────────────── */

    private JPanel buildLoginTab(LoginHandler handler, Runnable showRegister, Runnable showForgot) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(24, 32, 24, 32));

        JLabel tabTitle = new JLabel(I18n.t("login.button"));
        tabTitle.setFont(tabTitle.getFont().deriveFont(Font.BOLD, 18f));
        tabTitle.setForeground(new Color(51, 51, 51));
        tabTitle.setAlignmentX(0f);
        p.add(tabTitle);

        JPanel underline = new JPanel();
        underline.setBackground(PRIMARY);
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(0f);
        p.add(Box.createVerticalStrut(6));
        p.add(underline);

        p.add(Box.createVerticalStrut(24));

        PlaceholderTextField accountField = new PlaceholderTextField(20, I18n.t("login.placeholder.account"));
        styleInput(accountField);
        // keep reference for clearCredentials()
        this.accountField = accountField;
        JPanel accRow = inputWithPlaceholder(accountField, I18n.t("login.account"));
        accRow.setAlignmentX(0f);
        p.add(accRow);

        p.add(Box.createVerticalStrut(16));

        PlaceholderPasswordField passField = new PlaceholderPasswordField(20, I18n.t("login.placeholder.password"));
        styleInput(passField);
        // keep reference for clearCredentials()
        this.passField = passField;
        JPanel passRow = inputWithPlaceholder(passField, I18n.t("login.password"));
        passRow.setAlignmentX(0f);
        p.add(passRow);

        p.add(Box.createVerticalStrut(24));

        JButton loginBtn = new JButton(I18n.t("login.button"));
        stylePrimaryButton(loginBtn);
        loginBtn.setAlignmentX(0f);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(loginBtn);

        p.add(Box.createVerticalStrut(14));

        JPanel links = new JPanel(new BorderLayout());
        links.setOpaque(false);
        links.setAlignmentX(0f);
        links.setMaximumSize(new Dimension(Integer.MAX_VALUE, 28));
        JButton forgotBtn = new JButton(I18n.t("login.forgot"));
        JButton registerBtn = new JButton(I18n.t("login.tab.register"));
        styleLinkButton(forgotBtn);
        styleLinkButton(registerBtn);
        links.add(forgotBtn, BorderLayout.WEST);
        links.add(registerBtn, BorderLayout.EAST);
        p.add(links);

        accountField.addActionListener(e -> loginBtn.doClick());
        passField.addActionListener(e -> loginBtn.doClick());

        loginBtn.addActionListener(e -> {
            String account = accountField.getText().trim();
            String password = new String(passField.getPassword());

            if (account.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.account.password.required"));
                return;
            }

            java.util.Optional<String> roleName = data.authenticateAndGetRole(account, password);
            if (!roleName.isPresent()) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.login.failed"));
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
            if (showForgot != null) showForgot.run();
        });

        registerBtn.addActionListener(e -> {
            if (showRegister != null) showRegister.run();
        });

        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        gc.anchor = GridBagConstraints.CENTER;
        wrapper.add(p, gc);
        return wrapper;
    }

    /* ── Forgot password tab ─────────────────────────────── */

    private JPanel buildForgotTab(Runnable showLogin) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        JLabel tabTitle = new JLabel(I18n.t("forgot.title"));
        tabTitle.setFont(tabTitle.getFont().deriveFont(Font.BOLD, 18f));
        tabTitle.setForeground(new Color(51, 51, 51));
        tabTitle.setAlignmentX(0f);
        p.add(tabTitle);

        JPanel underline = new JPanel();
        underline.setBackground(PRIMARY);
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(0f);
        p.add(Box.createVerticalStrut(4));
        p.add(underline);
        p.add(Box.createVerticalStrut(14));

        JComboBox<Role> roleBox = new JComboBox<Role>(Role.values());
        styleInput(roleBox);

        JLabel roleLabel = new JLabel(I18n.t("common.role"));
        roleLabel.setFont(roleLabel.getFont().deriveFont(Font.PLAIN, 12f));
        roleLabel.setForeground(new Color(102, 102, 102));
        roleLabel.setAlignmentX(0f);
        p.add(roleLabel);
        p.add(Box.createVerticalStrut(4));

        JPanel roleRow = new JPanel(new BorderLayout());
        roleRow.setOpaque(false);
        roleRow.setAlignmentX(0f);
        roleRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        roleRow.add(roleBox, BorderLayout.CENTER);
        p.add(roleRow);
        p.add(Box.createVerticalStrut(10));

        JTextField accountField = new JTextField(20);
        JTextField verifyField = new JTextField(20);
        JPasswordField newPassField = new JPasswordField(20);
        JPasswordField newPass2Field = new JPasswordField(20);

        for (JTextField tf : new JTextField[] { accountField, verifyField, newPassField, newPass2Field }) {
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

        p.add(Box.createVerticalStrut(10));

        JButton backBtn = new JButton(I18n.t("common.back"));
        styleLinkButton(backBtn);
        backBtn.setAlignmentX(0f);
        p.add(backBtn);

        JScrollPane sp = new JScrollPane(p);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        wrapper.add(sp, BorderLayout.CENTER);

        backBtn.addActionListener(e -> {
            if (showLogin != null) showLogin.run();
        });

        resetBtn.addActionListener(e -> {
            Role role = (Role) roleBox.getSelectedItem();
            if (role == null) role = Role.TA;

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

            OperationLog.append(data.tempOperationFile(), "INFO", "Password reset for role=" + role.authRole() + ", account=" + account);
            JOptionPane.showMessageDialog(this, I18n.t("msg.reset.success"));
            if (showLogin != null) showLogin.run();
        });

        return wrapper;
    }

    /* ── Register tab ────────────────────────────────────── */

    private JPanel buildRegisterTab(Runnable showLogin) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);

        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBorder(BorderFactory.createEmptyBorder(20, 32, 20, 32));

        JLabel tabTitle = new JLabel(I18n.t("register.button"));
        tabTitle.setFont(tabTitle.getFont().deriveFont(Font.BOLD, 18f));
        tabTitle.setForeground(new Color(51, 51, 51));
        tabTitle.setAlignmentX(0f);
        p.add(tabTitle);

        JPanel underline = new JPanel();
        underline.setBackground(PRIMARY);
        underline.setMaximumSize(new Dimension(40, 3));
        underline.setAlignmentX(0f);
        p.add(Box.createVerticalStrut(4));
        p.add(underline);
        p.add(Box.createVerticalStrut(14));

        JTextField accountField = new JTextField(20);
        JTextField nameField = new JTextField(20);
        JTextField emailField = new JTextField(20);
        JPasswordField passField = new JPasswordField(20);
        JPasswordField pass2Field = new JPasswordField(20);
        JTextField skillsField = new JTextField(20);
        JTextField cvPathField = new JTextField(20);

        for (JTextField tf : new JTextField[]{accountField, nameField, emailField,
                passField, pass2Field, skillsField, cvPathField}) {
            styleInput(tf);
        }

        addField(p, accountField, I18n.t("register.account"));
        addField(p, nameField, I18n.t("register.name"));
        addField(p, emailField, I18n.t("register.email"));
        addField(p, passField, I18n.t("register.password"));
        addField(p, pass2Field, I18n.t("register.password2"));
        addField(p, skillsField, I18n.t("register.skills"));

        JPanel cvRow = new JPanel(new BorderLayout(6, 0));
        cvRow.setOpaque(false);
        cvRow.setAlignmentX(0f);
        cvRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        cvPathField.setPreferredSize(new Dimension(0, 32));
        JButton browse = new JButton(I18n.t("register.browse"));
        browse.setPreferredSize(new Dimension(70, 32));
        cvRow.add(cvPathField, BorderLayout.CENTER);
        cvRow.add(browse, BorderLayout.EAST);

        JLabel cvLabel = new JLabel(I18n.t("register.cv"));
        cvLabel.setFont(cvLabel.getFont().deriveFont(Font.PLAIN, 12f));
        cvLabel.setForeground(new Color(102, 102, 102));
        cvLabel.setAlignmentX(0f);
        p.add(cvLabel);
        p.add(Box.createVerticalStrut(4));
        p.add(cvRow);
        p.add(Box.createVerticalStrut(10));

        JCheckBox agreeBox = new JCheckBox(I18n.t("register.agree"));
        agreeBox.setOpaque(false);
        agreeBox.setAlignmentX(0f);
        p.add(agreeBox);
        p.add(Box.createVerticalStrut(14));

        JButton registerBtn = new JButton(I18n.t("register.button"));
        stylePrimaryButton(registerBtn);
        registerBtn.setAlignmentX(0f);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        p.add(registerBtn);

        p.add(Box.createVerticalStrut(10));

        JButton backBtn = new JButton(I18n.t("login.tab.login"));
        styleLinkButton(backBtn);
        backBtn.setAlignmentX(0f);
        p.add(backBtn);

        JScrollPane sp = new JScrollPane(p);
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        wrapper.add(sp, BorderLayout.CENTER);

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
                JOptionPane.showMessageDialog(this, I18n.t("msg.fields.required"));
                return;
            }
            if (!pass.equals(pass2)) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.password.mismatch"));
                return;
            }

            int minLen = data.getConfig().passwordMinLength();
            if (pass.length() < minLen) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.password.tooshort", minLen));
                return;
            }
            if (!agreeBox.isSelected()) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.agree.required"));
                return;
            }
            if (data.getApplicant(account).isPresent()) {
                JOptionPane.showMessageDialog(this, I18n.t("msg.account.exists"));
                return;
            }
            if (!isSupportedCv(cvPath, data.getConfig().cvFormats())) {
                JOptionPane.showMessageDialog(this, I18n.t("ta.profile.unsupported.cv") + data.getConfig().cvFormats());
                return;
            }

            String storedCvPath;
            try {
                storedCvPath = data.storeCv(account, cvPath);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, I18n.t("ta.profile.cv.savefailed"));
                return;
            }

            Applicant a = data.upsertApplicantByAccount(account, name, email, skills, storedCvPath);
            data.upsertUser(Role.TA.authRole(), a.id(), pass, a.name());
            OperationLog.append(data.tempOperationFile(), "INFO",
                    "actor=" + a.id() + " action=registerTa role=TA account=" + a.id());
            JOptionPane.showMessageDialog(this, I18n.t("msg.register.success"));
            if (showLogin != null) showLogin.run();
        });

        return wrapper;
    }

    /* ── Helpers ──────────────────────────────────────────── */

    private static void addField(JPanel p, JTextField tf, String label) {
        JLabel lbl = new JLabel(label);
        lbl.setFont(lbl.getFont().deriveFont(Font.PLAIN, 12f));
        lbl.setForeground(new Color(102, 102, 102));
        lbl.setAlignmentX(0f);
        p.add(lbl);
        p.add(Box.createVerticalStrut(4));
        JPanel row = inputWithPlaceholder(tf, label);
        row.setAlignmentX(0f);
        p.add(row);
        p.add(Box.createVerticalStrut(10));
    }

    private static JPanel inputWithPlaceholder(JTextField tf, String placeholder) {
        tf.putClientProperty("placeholder", placeholder);
        JPanel row = new JPanel(new BorderLayout());
        row.setOpaque(false);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        tf.setPreferredSize(new Dimension(0, 36));
        row.add(tf, BorderLayout.CENTER);
        return row;
    }

    private static void styleInput(JTextField tf) {
        UiTheme.styleTextField(tf);
        tf.setFont(tf.getFont().deriveFont(Font.PLAIN, 14f));
    }

    private static void styleInput(JComboBox<?> cb) {
        UiTheme.styleCombo(cb);
        cb.setFont(cb.getFont().deriveFont(Font.PLAIN, 14f));
    }

    private static void stylePrimaryButton(JButton b) {
        UiTheme.stylePrimaryButton(b);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 15f));
        b.setPreferredSize(new Dimension(0, 42));
    }

    private static void styleLinkButton(JButton b) {
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(false);
        b.setForeground(LINK_COLOR);
        b.setFont(b.getFont().deriveFont(Font.BOLD, 12f));
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

    private static final class PlaceholderTextField extends JTextField {
        private final String placeholder;

        PlaceholderTextField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getText().isEmpty() && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(160, 160, 160, 102));
                    g2.setFont(getFont());
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, x, y);
                } finally {
                    g2.dispose();
                }
            }
        }
    }

    private static final class PlaceholderPasswordField extends JPasswordField {
        private final String placeholder;

        PlaceholderPasswordField(int columns, String placeholder) {
            super(columns);
            this.placeholder = placeholder;
            addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent e) { repaint(); }
                @Override public void focusLost(java.awt.event.FocusEvent e) { repaint(); }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (getPassword().length == 0 && placeholder != null) {
                Graphics2D g2 = (Graphics2D) g.create();
                try {
                    g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                    g2.setColor(new Color(160, 160, 160, 102));
                    g2.setFont(getFont());
                    java.awt.FontMetrics fm = g2.getFontMetrics();
                    int x = getInsets().left;
                    int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                    g2.drawString(placeholder, x, y);
                } finally {
                    g2.dispose();
                }
            }
        }
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
