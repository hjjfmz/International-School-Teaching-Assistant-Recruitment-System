package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

public final class TaProfilePage extends JPanel {
    private final DataService data;
    private final String account;

    private final JTextField idField = new JTextField(18);
    private final JTextField nameField = new JTextField(18);
    private final JTextField emailField = new JTextField(18);
    private final JTextField skillsField = new JTextField(18);
    private final JTextField cvField = new JTextField(18);

    public TaProfilePage(DataService data, String account) {
        super(new BorderLayout());
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Profile"));

        idField.setEditable(false);
        nameField.setEditable(false);
        cvField.setEditable(false);

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Account (Student ID)"), c);
        c.gridx = 1; c.gridy = 0; form.add(idField, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Name"), c);
        c.gridx = 1; c.gridy = 1; form.add(nameField, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Email"), c);
        c.gridx = 1; c.gridy = 2; form.add(emailField, c);

        c.gridx = 0; c.gridy = 3; form.add(new JLabel("Skills"), c);
        c.gridx = 1; c.gridy = 3; form.add(skillsField, c);

        c.gridx = 0; c.gridy = 4; form.add(new JLabel("CV path"), c);
        c.gridx = 1; c.gridy = 4; form.add(cvField, c);

        JButton save = new JButton("Save");
        save.addActionListener(e -> save());
        c.gridx = 1; c.gridy = 5; form.add(save, c);

        add(form, BorderLayout.NORTH);

        load();
    }

    public void load() {
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        idField.setText(a.id());
        nameField.setText(a.name());
        emailField.setText(a.email());
        skillsField.setText(a.skills());
        cvField.setText(a.cvPath());
    }

    private void save() {
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;

        String email = emailField.getText().trim();
        String skills = skillsField.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required");
            return;
        }

        Applicant updated = a.withProfile(a.name(), email, skills, a.cvPath());
        if (updated.email().equals(a.email()) && updated.skills().equals(a.skills())) {
            JOptionPane.showMessageDialog(this, "No changes detected");
            return;
        }

        data.upsertApplicant(updated);
        JOptionPane.showMessageDialog(this, "Profile updated");
        load();
    }
}
