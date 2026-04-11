package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class AdminConfigPage extends JPanel {
    private final DataService data;
    private final String actor;

    // ① 数据目录：保留文本框
    private final JTextField dataPath = new JTextField(28);

    // ② 密码最小长度：下拉 1-8
    private final JComboBox<Integer> passwordMinLength = new JComboBox<>(new Integer[]{1, 2, 3, 4, 5, 6, 7, 8});

    // ③ CV 格式：三个复选框
    private final JCheckBox cvPdf  = new JCheckBox("pdf");
    private final JCheckBox cvDoc  = new JCheckBox("doc");
    private final JCheckBox cvDocx = new JCheckBox("docx");

    // ④ 默认语言：下拉 EN / ZH
    private final JComboBox<String> defaultLang = new JComboBox<>(new String[]{"EN", "\u4e2d\u6587 (ZH)"});

    public AdminConfigPage(DataService data, String actor) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.actor = actor == null ? "" : actor;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(I18n.t("admin.config.title")));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        // ① 数据目录
        c.gridx = 0; c.gridy = 0; form.add(new JLabel(I18n.t("admin.config.datapath")), c);
        c.gridx = 1; c.gridy = 0; form.add(dataPath, c);
        JButton browse = new JButton(I18n.t("common.browse"));
        c.gridx = 2; c.gridy = 0; form.add(browse, c);

        // hint 提示行
        JLabel hint = new JLabel(I18n.t("admin.config.datapath.hint"));
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 11f));
        hint.setForeground(new Color(120, 120, 120));
        c.gridx = 1; c.gridy = 1; c.gridwidth = 2; form.add(hint, c);
        c.gridwidth = 1;

        // ② 密码最小长度
        c.gridx = 0; c.gridy = 2; form.add(new JLabel(I18n.t("admin.config.pwdlen")), c);
        c.gridx = 1; c.gridy = 2; form.add(passwordMinLength, c);

        // ③ CV 格式
        c.gridx = 0; c.gridy = 3; form.add(new JLabel(I18n.t("admin.config.cvformats")), c);
        JPanel cvPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        cvPanel.setOpaque(false);
        cvPanel.add(cvPdf);
        cvPanel.add(cvDoc);
        cvPanel.add(cvDocx);
        c.gridx = 1; c.gridy = 3; c.gridwidth = 2; form.add(cvPanel, c);
        c.gridwidth = 1;

        // ④ 默认语言
        c.gridx = 0; c.gridy = 4; form.add(new JLabel(I18n.t("admin.config.lang")), c);
        c.gridx = 1; c.gridy = 4; form.add(defaultLang, c);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton reload = new JButton(I18n.t("common.reload"));
        JButton save = new JButton(I18n.t("common.save"));
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

        // ① 数据目录
        dataPath.setText(cfg.dataPath());

        // ② 密码最小长度
        int pml = cfg.passwordMinLength();
        if (pml < 1) pml = 1;
        if (pml > 8) pml = 8;
        passwordMinLength.setSelectedItem(pml);

        // ③ CV 格式
        String fmts = cfg.cvFormats() == null ? "" : cfg.cvFormats().toLowerCase();
        cvPdf.setSelected(fmts.contains("pdf"));
        cvDoc.setSelected(fmts.contains("doc") && !fmts.contains("docx") || fmts.contains("doc,") || fmts.startsWith("doc"));
        cvDocx.setSelected(fmts.contains("docx"));
        // 重新精确解析，避免 "docx" 影响 "doc" 判断
        boolean hasPdf = false, hasDoc = false, hasDocx = false;
        for (String s : fmts.split(",")) {
            String t = s.trim();
            if (t.equals("pdf"))  hasPdf  = true;
            if (t.equals("doc"))  hasDoc  = true;
            if (t.equals("docx")) hasDocx = true;
        }
        cvPdf.setSelected(hasPdf);
        cvDoc.setSelected(hasDoc);
        cvDocx.setSelected(hasDocx);

        // ④ 默认语言
        String lang = cfg.defaultLang() == null ? "EN" : cfg.defaultLang().trim().toUpperCase();
        defaultLang.setSelectedItem(lang.equals("ZH") ? "\u4e2d\u6587 (ZH)" : "EN");
    }

    private void save() {
        if (data == null) return;
        String p = dataPath.getText().trim();

        // ② 密码最小长度
        int pml = (Integer) passwordMinLength.getSelectedItem();

        // ③ CV 格式
        List<String> fmtList = new ArrayList<>();
        if (cvPdf.isSelected())  fmtList.add("pdf");
        if (cvDoc.isSelected())  fmtList.add("doc");
        if (cvDocx.isSelected()) fmtList.add("docx");
        String formats = fmtList.isEmpty() ? "pdf,doc,docx" : String.join(",", fmtList);

        // ④ 默认语言
        String langItem = (String) defaultLang.getSelectedItem();
        String lang = (langItem != null && langItem.startsWith("\u4e2d")) ? "ZH" : "EN";

        boolean ok = data.updateConfig(actor, new DataService.Config(p, pml, formats, lang));
        if (!ok) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.save.failed"));
            return;
        }
        JOptionPane.showMessageDialog(this, I18n.t("admin.config.saved"));
        load();
    }
}
