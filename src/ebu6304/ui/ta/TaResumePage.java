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

import ebu6304.ai.controller.AiIndexController;
import ebu6304.model.Applicant;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class TaResumePage extends JPanel {
    private final DataService data;
    private final String account;
    private final AiIndexController aiIndexController;

    private final JTextField cvField = new JTextField(26);

    public TaResumePage(DataService data, String account, AiIndexController aiIndexController) {
        super(new BorderLayout());
        this.data = data;
        this.account = account;
        this.aiIndexController = aiIndexController;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(I18n.t("ta.resume.title")));

        cvField.setEditable(false);

        JButton browse = new JButton(I18n.t("ta.resume.reupload"));
        JButton open = new JButton(I18n.t("ta.resume.opencv"));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel(I18n.t("ta.resume.currentcv")), c);
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
        if (!isSupported(path, data.getConfig().cvFormats())) {
            JOptionPane.showMessageDialog(this, I18n.t("ta.profile.unsupported.cv") + data.getConfig().cvFormats());
            return;
        }

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;

        String storedCvPath;
        try {
            storedCvPath = data.storeCv(a.id(), path);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("ta.profile.cv.savefailed"));
            return;
        }

        Applicant updated = a.withProfile(a.name(), a.email(), a.skills(), storedCvPath);
        data.upsertApplicant(updated);
        if (aiIndexController != null) aiIndexController.refreshApplicant(account);
        JOptionPane.showMessageDialog(this, I18n.t("msg.upload.success"));
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
            JOptionPane.showMessageDialog(this, I18n.t("ta.resume.openfailed"));
        }
    }

    private static boolean isSupported(String path, String formatsCsv) {
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
