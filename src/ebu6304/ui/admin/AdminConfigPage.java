package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ebu6304.storage.DataService;

public final class AdminConfigPage extends JPanel {
    private final DataService data;
    private final String actor;

    private final JTextField dataPath = new JTextField(28);
    private final JTextField passwordMinLength = new JTextField(6);
    private final JTextField cvFormats = new JTextField(18);
    private final JTextField defaultLang = new JTextField(6);

    public AdminConfigPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("System Config"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Data path (effective next startup):"), c);
        c.gridx = 1; c.gridy = 0; form.add(dataPath, c);
        JButton browse = new JButton("Browse");
        c.gridx = 2; c.gridy = 0; form.add(browse, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Password min length (TA register):"), c);
        c.gridx = 1; c.gridy = 1; form.add(passwordMinLength, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel("CV formats (comma-separated):"), c);
        c.gridx = 1; c.gridy = 2; form.add(cvFormats, c);

        c.gridx = 0; c.gridy = 3; form.add(new JLabel("Default language (EN):"), c);
        c.gridx = 1; c.gridy = 3; form.add(defaultLang, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reload = new JButton("Reload");
        JButton save = new JButton("Save");
        actions.add(reload);
        actions.add(save);

        browse.addActionListener(e -> chooseDir());
        reload.addActionListener(e -> load());
        save.addActionListener(e -> save());

        add(form, BorderLayout.NORTH);
        add(actions, BorderLayout.SOUTH);

        load();
    }

    private void chooseDir() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        if (chooser.getSelectedFile() == null) return;
        dataPath.setText(chooser.getSelectedFile().getAbsolutePath());
    }

    private void load() {
        if (data == null) return;
        DataService.Config cfg = data.getConfig();
        dataPath.setText(cfg.dataPath());
        passwordMinLength.setText(String.valueOf(cfg.passwordMinLength()));
        cvFormats.setText(cfg.cvFormats());
        defaultLang.setText(cfg.defaultLang());
    }

    private void save() {
        if (data == null) return;
        String p = dataPath.getText().trim();
        String pmlRaw = passwordMinLength.getText().trim();
        String formats = cvFormats.getText().trim();
        String lang = defaultLang.getText().trim();
        int pml = 6;
        try {
            if (!pmlRaw.isEmpty()) pml = Integer.parseInt(pmlRaw);
        } catch (NumberFormatException nfe) {
            JOptionPane.showMessageDialog(this, "Password min length must be a number");
            return;
        }
        if (pml <= 0) pml = 6;
        if (formats.isEmpty()) formats = "pdf,doc,docx";
        if (lang.isEmpty()) lang = "EN";

        boolean ok = data.updateConfig(actor, new DataService.Config(p, pml, formats, lang));
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Save failed");
            return;
        }
        JOptionPane.showMessageDialog(this, "Saved. Data path changes take effect on next startup.");
        load();
    }
}
