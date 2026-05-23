package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.HierarchyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import ebu6304.storage.DataService;
import ebu6304.ui.I18n;
import ebu6304.ui.UiTheme;

public final class AdminLogPage extends JPanel {
    private final DataService data;

    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final DefaultTableModel model = new DefaultTableModel(new String[] { "Time", "Level", "Actor", "Action", "Detail" }, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    };
    private final JTable table = new JTable(model);

    private final JButton prevBtn = new JButton("Prev");
    private final JButton nextBtn = new JButton("Next");
    private final JLabel pageLabel = new JLabel();
    private final javax.swing.JComboBox<Integer> pageSizeBox = new javax.swing.JComboBox<Integer>(new Integer[] { 20, 50, 100, 200 });

    private List<LogEntry> all = new ArrayList<LogEntry>();
    private List<LogEntry> filtered = new ArrayList<LogEntry>();
    private int page = 1;
    private int totalPages = 1;

    private final JTextField actor = new JTextField(10);
    private final JTextField action = new JTextField(10);
    private final JTextField level = new JTextField(8);
    private final JTextField keyword = new JTextField(10);

    public AdminLogPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        setBorder(BorderFactory.createTitledBorder(I18n.t("admin.logs.title")));

        UiTheme.styleTable(table);

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
        JScrollPane sp = new JScrollPane(table);
        UiTheme.styleScrollPane(sp);
        sp.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);

        JPanel pager = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        pager.add(new JLabel("Page size"));
        pager.add(pageSizeBox);
        pager.add(prevBtn);
        pager.add(pageLabel);
        pager.add(nextBtn);

        JPanel center = new JPanel(new BorderLayout(0, 6));
        center.add(sp, BorderLayout.CENTER);
        center.add(pager, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        pageSizeBox.setSelectedItem(Integer.valueOf(50));
        pageSizeBox.addActionListener(e -> {
            page = 1;
            refreshPage();
        });
        prevBtn.addActionListener(e -> {
            if (page > 1) {
                page--;
                refreshPage();
            }
        });
        nextBtn.addActionListener(e -> {
            if (page < totalPages) {
                page++;
                refreshPage();
            }
        });

        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
                    int viewRow = table.rowAtPoint(e.getPoint());
                    int viewCol = table.columnAtPoint(e.getPoint());
                    if (viewRow < 0) return;
                    if (viewCol < 0) return;
                    int modelRow = table.convertRowIndexToModel(viewRow);
                    int modelCol = table.convertColumnIndexToModel(viewCol);
                    String colName = model.getColumnName(modelCol);
                    String val = String.valueOf(model.getValueAt(modelRow, modelCol));
                    showCellDialog(colName, val);
                }
            }
        });

        addHierarchyListener(e -> {
            if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && isShowing()) {
                refresh();
            }
        });

        refresh();
    }

    public void refresh() {
        try {
            List<String> lines = Files.readAllLines(logFile(), StandardCharsets.UTF_8);

            all = new ArrayList<LogEntry>();
            for (String l : lines) {
                if (l == null) continue;
                String[] parts = l.split("\\t", 3);
                String rawTime = parts.length >= 1 ? parts[0] : "";
                String lv = parts.length >= 2 ? parts[1] : "";
                String msg = parts.length >= 3 ? parts[2] : "";
                String a = extractKeyValue(msg, "actor");
                String ac = extractKeyValue(msg, "action");
                all.add(new LogEntry(rawTime, formatTimeToSeconds(rawTime), lv, a, ac, msg));
            }

            page = 1;
            refreshPage();
        } catch (IOException e) {
            all = new ArrayList<LogEntry>();
            filtered = new ArrayList<LogEntry>();
            page = 1;
            refreshPage();
        }
    }

    private void refreshPage() {
        filtered = filter(all);

        int pageSize = ((Integer) pageSizeBox.getSelectedItem()).intValue();
        totalPages = Math.max(1, (int) Math.ceil((double) filtered.size() / (double) pageSize));
        if (page > totalPages) page = totalPages;
        if (page < 1) page = 1;

        int from = (page - 1) * pageSize;
        int to = Math.min(filtered.size(), from + pageSize);

        model.setRowCount(0);
        for (int i = from; i < to; i++) {
            LogEntry e = filtered.get(i);
            model.addRow(new Object[] { e.time, e.level, e.actor, e.action, e.detail });
        }

        pageLabel.setText("Page " + page + "/" + totalPages + " (" + filtered.size() + ")");
        prevBtn.setEnabled(page > 1);
        nextBtn.setEnabled(page < totalPages);
    }

    private List<LogEntry> filter(List<LogEntry> src) {
        final String actorFilter = actor.getText().trim().toLowerCase();
        final String actionFilter = action.getText().trim().toLowerCase();
        final String levelFilter = level.getText().trim().toLowerCase();
        final String keywordFilter = keyword.getText().trim().toLowerCase();

        List<LogEntry> out = new ArrayList<LogEntry>();
        for (LogEntry e : src) {
            if (e == null) continue;
            if (!levelFilter.isEmpty() && !safe(e.level).toLowerCase().contains(levelFilter)) continue;
            if (!actorFilter.isEmpty() && !safe(e.actor).toLowerCase().contains(actorFilter)) continue;
            if (!actionFilter.isEmpty() && !safe(e.action).toLowerCase().contains(actionFilter)) continue;
            if (!keywordFilter.isEmpty()) {
                String hay = (safe(e.rawTime) + "\t" + safe(e.time) + "\t" + safe(e.level) + "\t" + safe(e.actor) + "\t" + safe(e.action) + "\t" + safe(e.detail)).toLowerCase();
                if (!hay.contains(keywordFilter)) continue;
            }
            out.add(e);
        }
        return out;
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle(I18n.t("admin.logs.export.title"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        try {
            List<String> out = new ArrayList<String>();
            out.add("Time\tLevel\tActor\tAction\tDetail");
            List<LogEntry> exp = filtered == null ? new ArrayList<LogEntry>() : filtered;
            for (LogEntry e : exp) {
                out.add(safe(e.rawTime) + "\t" + safe(e.level) + "\t" + safe(e.actor) + "\t" + safe(e.action) + "\t" + safe(e.detail));
            }
            Files.write(chooser.getSelectedFile().toPath(), out, StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
        }
    }

    private void clear() {
        int ok = javax.swing.JOptionPane.showConfirmDialog(this, I18n.t("admin.logs.confirm.clear"), I18n.t("common.confirm"), javax.swing.JOptionPane.YES_NO_OPTION);
        if (ok != javax.swing.JOptionPane.YES_OPTION) return;
        try {
            Files.write(logFile(), new byte[0], StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE);
        } catch (IOException e) {
        }
        refresh();
    }

    private Path logFile() {
        return data.tempOperationFile();
    }

    private static String extractKeyValue(String msg, String key) {
        if (msg == null) return "";
        String needle = key + "=";
        int idx = msg.indexOf(needle);
        if (idx < 0) return "";
        int start = idx + needle.length();
        int end = msg.indexOf(' ', start);
        if (end < 0) end = msg.length();
        return msg.substring(start, end);
    }

    private static final class ZebraRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
                int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(250, 250, 252));
                c.setForeground(new Color(30, 41, 59));
            }
            if (value != null) {
                setToolTipText(String.valueOf(value));
            } else {
                setToolTipText(null);
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    private void showCellDialog(String title, String content) {
        JTextArea ta = new JTextArea(content == null ? "" : content);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(ta);
        sp.setPreferredSize(new Dimension(800, 400));
        JOptionPane.showMessageDialog(this, sp, title == null ? "" : title, JOptionPane.INFORMATION_MESSAGE);
    }

    private static String formatTimeToSeconds(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        if (s.isEmpty()) return "";

        // epoch millis
        boolean digits = true;
        for (int i = 0; i < s.length(); i++) {
            char ch = s.charAt(i);
            if (ch < '0' || ch > '9') {
                digits = false;
                break;
            }
        }
        if (digits) {
            try {
                long ms = Long.parseLong(s);
                LocalDateTime dt = LocalDateTime.ofInstant(Instant.ofEpochMilli(ms), ZoneId.systemDefault());
                return DISPLAY_TIME.format(dt);
            } catch (RuntimeException ignore) {
            }
        }

        // ISO-like: 2026-04-11T01:18:34.192891200 or 2026-04-11 01:18:34.123
        String normalized = s.replace('T', ' ');
        int dot = normalized.indexOf('.');
        if (dot >= 0) normalized = normalized.substring(0, dot);
        if (normalized.length() >= 19) {
            return normalized.substring(0, 19);
        }
        return normalized;
    }

    private static final class LogEntry {
        private final String rawTime;
        private final String time;
        private final String level;
        private final String actor;
        private final String action;
        private final String detail;

        private LogEntry(String rawTime, String time, String level, String actor, String action, String detail) {
            this.rawTime = rawTime;
            this.time = time;
            this.level = level;
            this.actor = actor;
            this.action = action;
            this.detail = detail;
        }
    }
}
