package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class AdminLogPage extends JPanel {
    private final DataService data;
    private final JTextArea area = new JTextArea();

    private final JTextField actor = new JTextField(10);
    private final JTextField action = new JTextField(10);
    private final JTextField level = new JTextField(8);
    private final JTextField keyword = new JTextField(10);

    public AdminLogPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        setBorder(BorderFactory.createTitledBorder(I18n.t("admin.logs.title")));

        area.setEditable(false);

        JPanel top = new JPanel(new BorderLayout(10, 10));
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filters.add(new JLabel(I18n.t("admin.logs.actor")));
        filters.add(actor);
        filters.add(new JLabel(I18n.t("admin.logs.action")));
        filters.add(action);
        filters.add(new JLabel(I18n.t("admin.logs.level")));
        filters.add(level);
        filters.add(new JLabel(I18n.t("admin.logs.keyword")));
        filters.add(keyword);

        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton export = new JButton(I18n.t("common.export"));
        JButton clear = new JButton(I18n.t("common.clear"));
        actionsPanel.add(refresh);
        actionsPanel.add(export);
        actionsPanel.add(clear);

        JPanel topContent = new JPanel(new BorderLayout(10, 5));
        topContent.add(filters, BorderLayout.NORTH);
        topContent.add(actionsPanel, BorderLayout.SOUTH);
        top.add(topContent, BorderLayout.CENTER);

        refresh.addActionListener(e -> refresh());
        export.addActionListener(e -> export());
        clear.addActionListener(e -> clear());

        actor.addActionListener(e -> refresh());
        action.addActionListener(e -> refresh());
        level.addActionListener(e -> refresh());
        keyword.addActionListener(e -> refresh());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<String> lines = Files.readAllLines(data.dataDir().resolve("temp_operation.txt"), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();

            String actorFilter = actor.getText().trim();
            String actionFilter = action.getText().trim();
            String levelFilter = level.getText().trim();
            String keywordFilter = keyword.getText().trim();

            for (String l : lines) {
                if (l == null) continue;
                String line = l;

                String[] parts = line.split("\\t", 3);
                String lv = parts.length >= 2 ? parts[1] : "";
                String msg = parts.length >= 3 ? parts[2] : "";

                if (!levelFilter.isEmpty() && !lv.toLowerCase().contains(levelFilter.toLowerCase())) continue;
                if (!actorFilter.isEmpty() && !containsKeyValue(msg, "actor", actorFilter)) continue;
                if (!actionFilter.isEmpty() && !containsKeyValue(msg, "action", actionFilter)) continue;

                if (!keywordFilter.isEmpty()) {
                    String hay = line.toLowerCase();
                    if (!hay.contains(keywordFilter.toLowerCase())) continue;
                }

                sb.append(line).append("\n");
            }
            area.setText(sb.toString());
        } catch (IOException e) {
            area.setText("" );
        }
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.t("admin.logs.export.title"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        try {
            Files.write(chooser.getSelectedFile().toPath(), area.getText().getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
        }
    }

    private void clear() {
        int ok = javax.swing.JOptionPane.showConfirmDialog(this, I18n.t("admin.logs.confirm.clear"), I18n.t("common.confirm"), javax.swing.JOptionPane.YES_NO_OPTION);
        if (ok != javax.swing.JOptionPane.YES_OPTION) return;
        try {
            Files.write(data.dataDir().resolve("temp_operation.txt"), new byte[0], StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
        }
        refresh();
    }

    private static boolean containsKeyValue(String msg, String key, String expected) {
        if (msg == null) return false;
        if (expected == null) expected = "";
        String needle = key + "=";
        int idx = msg.indexOf(needle);
        if (idx < 0) return false;
        int start = idx + needle.length();
        int end = msg.indexOf(' ', start);
        if (end < 0) end = msg.length();
        String value = msg.substring(start, end);
        return value.toLowerCase().contains(expected.toLowerCase());
    }
}