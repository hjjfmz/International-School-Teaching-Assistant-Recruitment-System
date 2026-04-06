package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class MoPostJobPage extends JPanel {
    private final DataService data;
    private final String account;

    private final JTextField titleField = new JTextField(22);
    private final JTextField skillsField = new JTextField(22);
    private final JTextField hoursField = new JTextField(6);
    private final JTextArea descArea = new JTextArea(6, 22);

    public MoPostJobPage(DataService data, String account) {
        super(new BorderLayout());
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(I18n.t("mo.post.title")));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel(I18n.t("mo.post.label.title")), c);
        c.gridx = 1; c.gridy = 0; form.add(titleField, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel(I18n.t("mo.post.label.skills")), c);
        c.gridx = 1; c.gridy = 1; form.add(skillsField, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel(I18n.t("mo.post.label.hours")), c);
        c.gridx = 1; c.gridy = 2; form.add(hoursField, c);

        c.gridx = 0; c.gridy = 3; form.add(new JLabel(I18n.t("mo.post.label.desc")), c);
        c.gridx = 1; c.gridy = 3; form.add(descArea, c);

        JPanel actions = new JPanel();
        JButton preview = new JButton(I18n.t("common.preview"));
        JButton submit = new JButton(I18n.t("common.submit"));
        actions.add(preview);
        actions.add(submit);

        c.gridx = 1; c.gridy = 4; form.add(actions, c);

        preview.addActionListener(e -> preview());
        submit.addActionListener(e -> submit());

        add(form, BorderLayout.NORTH);
    }

    private void preview() {
        String title = titleField.getText().trim();
        String skills = skillsField.getText().trim();
        String hours = hoursField.getText().trim();
        String desc = descArea.getText().trim();
        String msg = "Title: " + title + "\n" +
                "Required skills: " + skills + "\n" +
                "Hours/week: " + hours + "\n\n" +
                desc;
        JOptionPane.showMessageDialog(this, msg);
    }

    private void submit() {
        String title = titleField.getText().trim();
        String skills = skillsField.getText().trim();
        String hoursRaw = hoursField.getText().trim();
        String desc = descArea.getText().trim();

        if (title.isEmpty() || skills.isEmpty() || hoursRaw.isEmpty() || desc.isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.fields.required"));
            return;
        }
        int hours;
        try {
            hours = Integer.parseInt(hoursRaw);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, I18n.t("mo.post.hours.nan"));
            return;
        }

        data.createJob(title, desc, skills, hours, account);
        JOptionPane.showMessageDialog(this, I18n.t("mo.post.success"));

        titleField.setText("");
        skillsField.setText("");
        hoursField.setText("");
        descArea.setText("");
    }
}
