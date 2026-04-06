package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;
import ebu6304.storage.OperationLog;

public final class ForgotPasswordPanel extends JPanel {
    public interface BackHandler {
        void onBack();
    }

    private final DataService data;

    public ForgotPasswordPanel(DataService data, BackHandler back) {
        super(new BorderLayout());
        this.data = data;

        setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(I18n.t("forgot.title")));

        JComboBox<Role> roleBox = new JComboBox<Role>(Role.values());
        JTextField accountField = new JTextField(18);
        JTextField verifyField = new JTextField(18);
        JPasswordField newPassField = new JPasswordField(18);
        JPasswordField newPass2Field = new JPasswordField(18);

        JButton resetBtn = new JButton(I18n.t("forgot.button"));
        JButton backBtn = new JButton(I18n.t("common.back"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Role"), c);
        c.gridx = 1; c.gridy = 0; form.add(roleBox, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel(I18n.t("login.account")), c);
        c.gridx = 1; c.gridy = 1; form.add(accountField, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel(I18n.t("forgot.verify")), c);
        c.gridx = 1; c.gridy = 2; form.add(verifyField, c);

        c.gridx = 0; c.gridy = 3; form.add(new JLabel(I18n.t("forgot.newpass")), c);
        c.gridx = 1; c.gridy = 3; form.add(newPassField, c);

        c.gridx = 0; c.gridy = 4; form.add(new JLabel(I18n.t("forgot.newpass2")), c);
        c.gridx = 1; c.gridy = 4; form.add(newPass2Field, c);

        JPanel btns = new JPanel();
        btns.add(resetBtn);
        btns.add(backBtn);
        c.gridx = 1; c.gridy = 5; form.add(btns, c);

        resetBtn.addActionListener(e -> {
            Role role = (Role) roleBox.getSelectedItem();
            String account = accountField.getText().trim();
            String verify = verifyField.getText().trim();
            String np = new String(newPassField.getPassword());
            String np2 = new String(newPass2Field.getPassword());

            if (account.isEmpty() || np.isEmpty() || np2.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Account and new password are required");
                return;
            }
            if (!np.equals(np2)) {
                JOptionPane.showMessageDialog(this, "Passwords do not match");
                return;
            }

            if (role == Role.TA) {
                Applicant a = data.getApplicant(account).orElse(null);
                if (a == null) {
                    JOptionPane.showMessageDialog(this, "Account not found");
                    return;
                }
                if (verify.isEmpty() || !verify.equalsIgnoreCase(a.email())) {
                    JOptionPane.showMessageDialog(this, "Verification information mismatch");
                    return;
                }
            }

            boolean ok = data.resetPassword(role.authRole(), account, np);
            if (!ok) {
                JOptionPane.showMessageDialog(this, "Account not found or role mismatch");
                return;
            }

            OperationLog.append(data.tempOperationFile(), "INFO", "Password reset for role=" + role.authRole() + ", account=" + account);
            JOptionPane.showMessageDialog(this, "Password reset successful");
        });

        backBtn.addActionListener(e -> {
            if (back != null) back.onBack();
        });

        add(form, BorderLayout.CENTER);
    }
}
