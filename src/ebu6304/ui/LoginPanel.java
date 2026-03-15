package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

public final class LoginPanel extends JPanel {
    public interface LoginHandler {
        void onLogin(Role role, String account);
        void onForgotPassword();
    }

    private final DataService data;

    public LoginPanel(DataService data, LoginHandler handler) {
        super(new BorderLayout());
        this.data = data;
        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(I18n.t("login.tab.login"), buildLoginTab(handler));
        tabs.addTab(I18n.t("login.tab.register"), buildRegisterTab());

        add(tabs, BorderLayout.CENTER);
    }

    private JPanel buildLoginTab(LoginHandler handler) {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JComboBox<Role> roleBox = new JComboBox<Role>(Role.values());
        JTextField accountField = new JTextField(18);
        JPasswordField passField = new JPasswordField(18);

        JButton loginBtn = new JButton(I18n.t("login.button"));
        JButton forgotBtn = new JButton(I18n.t("login.forgot"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; p.add(new JLabel("Role"), c);
        c.gridx = 1; c.gridy = 0; p.add(roleBox, c);

        c.gridx = 0; c.gridy = 1; p.add(new JLabel(I18n.t("login.account")), c);
        c.gridx = 1; c.gridy = 1; p.add(accountField, c);

        c.gridx = 0; c.gridy = 2; p.add(new JLabel(I18n.t("login.password")), c);
        c.gridx = 1; c.gridy = 2; p.add(passField, c);

        JPanel btns = new JPanel();
        btns.add(loginBtn);
        btns.add(forgotBtn);

        c.gridx = 1; c.gridy = 3; p.add(btns, c);

        loginBtn.addActionListener(e -> {
            Role role = (Role) roleBox.getSelectedItem();
            String account = accountField.getText().trim();
            String password = new String(passField.getPassword());

            if (account.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Account and password are required");
                return;
            }

            boolean ok = data.authenticate(role.authRole(), account, password);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Account / password / role mismatch");
                return;
            }

            if (handler != null) handler.onLogin(role, account);
        });

        forgotBtn.addActionListener(e -> {
            if (handler != null) handler.onForgotPassword();
        });

        return p;
    }

    private JPanel buildRegisterTab() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

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
        c.gridx = 1; c.gridy = 8; p.add(registerBtn, c);

        browse.addActionListener(e -> {
            JFileChooser chooser = new JFileChooser();
            int res = chooser.showOpenDialog(this);
            if (res == JFileChooser.APPROVE_OPTION) {
                File f = chooser.getSelectedFile();
                if (f != null) cvPathField.setText(f.getAbsolutePath());
            }
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
            if (!agreeBox.isSelected()) {
                JOptionPane.showMessageDialog(this, "Please accept the registration terms");
                return;
            }
            if (data.getApplicant(account).isPresent()) {
                JOptionPane.showMessageDialog(this, "Account already exists");
                return;
            }
            if (!isSupportedCv(cvPath)) {
                JOptionPane.showMessageDialog(this, "Only PDF/Word formats are supported");
                return;
            }

            Applicant a = data.upsertApplicantByAccount(account, name, email, skills, cvPath);
            data.upsertUser(Role.TA.authRole(), a.id(), pass, a.name());
            JOptionPane.showMessageDialog(this, "Registration successful. Please login.");
        });

        return p;
    }

    private static boolean isSupportedCv(String path) {
        if (path == null) return false;
        String p = path.toLowerCase();
        return p.endsWith(".pdf") || p.endsWith(".doc") || p.endsWith(".docx");
    }
}
