package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JFileChooser;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

public final class TaResumePage extends JPanel {
    private final DataService data;
    private final String account;

    private final JTextField cvField = new JTextField(26);

    public TaResumePage(DataService data, String account) {
        super(new BorderLayout());
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder("Resume"));

        cvField.setEditable(false);

        JButton browse = new JButton("Re-upload");
        JButton open = new JButton("Open CV");

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Current CV path"), c);
        c.gridx = 1; c.gridy = 0; form.add(cvField, c);

        JPanel btns = new JPanel();
        btns.add(browse);
        btns.add(open);
        c.gridx = 1; c.gridy = 1; form.add(btns, c);

        browse.addActionListener(e -> upload());
        open.addActionListener(e -> open());

        add(form, BorderLayout.NORTH);
        load();
    }

    public void load() {
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        cvField.setText(a.cvPath());
    }

    private void upload() {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f == null) return;
        String path = f.getAbsolutePath();
        if (!isSupported(path)) {
            JOptionPane.showMessageDialog(this, "Only PDF/Word formats are supported");
            return;
        }

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        Applicant updated = a.withProfile(a.name(), a.email(), a.skills(), path);
        data.upsertApplicant(updated);
        JOptionPane.showMessageDialog(this, "Upload success");
        load();
    }

    private void open() {
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        String path = a.cvPath();
        if (path == null || path.trim().isEmpty()) return;
        try {
            java.awt.Desktop.getDesktop().open(new File(path));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open file");
        }
    }

    private static boolean isSupported(String path) {
        if (path == null) return false;
        String p = path.toLowerCase();
        return p.endsWith(".pdf") || p.endsWith(".doc") || p.endsWith(".docx");
    }
}
