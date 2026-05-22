package ebu6304.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JViewport;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.swing.plaf.basic.BasicComboPopup;
import javax.swing.plaf.basic.ComboPopup;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.plaf.basic.BasicTableHeaderUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

public final class UiTheme {
    public static final Color APP_BG = new Color(245, 247, 250);
    public static final Color CARD_BG = Color.WHITE;
    public static final Color TEXT = new Color(15, 23, 42);
    public static final Color MUTED = new Color(82, 99, 125);
    public static final Color BORDER = new Color(221, 228, 238);
    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color SUCCESS = new Color(16, 185, 129);
    public static final Color WARNING = new Color(245, 158, 11);
    public static final Color DANGER = new Color(220, 38, 38);
    public static final Color PURPLE = new Color(139, 92, 246);
    public static final Color CYAN = new Color(6, 182, 212);
    public static final Color ORANGE = new Color(255, 112, 36);

    private static final Color[] ACCENTS = { PRIMARY, SUCCESS, WARNING, PURPLE, CYAN, ORANGE };

    private UiTheme() {
    }

    public static void stylePage(JPanel panel) {
        if (panel == null) return;
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
    }

    public static Border cardBorder(int padding) {
        return BorderFactory.createEmptyBorder(padding, padding, padding, padding);
    }

    public static void stylePanelCard(JPanel panel, String title) {
        if (panel == null) return;
        panel.setOpaque(false);
        panel.setBackground(CARD_BG);
        panel.setBorder(new SectionBorder(title, accentFor(title)));
    }

    public static void styleTable(JTable table) {
        if (table == null) return;
        table.setRowHeight(42);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setFillsViewportHeight(true);
        table.setSelectionBackground(new Color(232, 242, 255));
        table.setSelectionForeground(TEXT);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT);
        table.setFont(table.getFont().deriveFont(Font.PLAIN, 13f));
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.setBorder(BorderFactory.createEmptyBorder());
        table.setOpaque(true);

        JTableHeader header = table.getTableHeader();
        if (header != null) {
            header.setReorderingAllowed(false);
            header.setOpaque(false);
            header.setBackground(CARD_BG);
            header.setForeground(new Color(51, 65, 85));
            header.setFont(header.getFont().deriveFont(Font.BOLD, 12f));
            header.setPreferredSize(new Dimension(header.getPreferredSize().width, 46));
            header.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
            header.setUI(new ModernTableHeaderUI());
            header.setDefaultRenderer(new ModernHeaderRenderer());
        }

        installAdaptiveColumns(table);
    }

    private static void installAdaptiveColumns(final JTable table) {
        if (table == null) return;
        if (Boolean.TRUE.equals(table.getClientProperty("uiTheme.columnsAdaptive"))) {
            bindViewportListener(table);
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    sizeColumnsToFit(table, 60);
                }
            });
            return;
        }

        table.putClientProperty("uiTheme.columnsAdaptive", Boolean.TRUE);
        table.getModel().addTableModelListener(e -> SwingUtilities.invokeLater(() -> sizeColumnsToFit(table, 60)));
        bindViewportListener(table);
        SwingUtilities.invokeLater(() -> sizeColumnsToFit(table, 60));
    }

    private static void bindViewportListener(final JTable table) {
        if (table == null) return;
        java.awt.Container parent = table.getParent();
        if (!(parent instanceof JViewport)) return;
        final JViewport viewport = (JViewport) parent;
        if (Boolean.TRUE.equals(viewport.getClientProperty("uiTheme.columnsAdaptiveBound"))) return;
        viewport.putClientProperty("uiTheme.columnsAdaptiveBound", Boolean.TRUE);
        viewport.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                SwingUtilities.invokeLater(() -> sizeColumnsToFit(table, 60));
            }
        });
    }

    private static void sizeColumnsToFit(JTable table, int sampleRows) {
        if (table == null || table.getColumnModel() == null) return;
        int columnCount = table.getColumnCount();
        if (columnCount <= 0) return;

        int rowCount = table.getRowCount();
        int inspectedRows = Math.min(Math.max(rowCount, 1), Math.max(1, sampleRows));
        int[] widths = new int[columnCount];
        int[] mins = new int[columnCount];
        int totalWidth = 0;

        for (int col = 0; col < columnCount; col++) {
            int headerWidth = measureHeaderWidth(table, col);
            int width = headerWidth + 24;
            for (int row = 0; row < inspectedRows && row < rowCount; row++) {
                width = Math.max(width, measureCellWidth(table, row, col) + 24);
            }
            int minWidth = Math.max(88, headerWidth + 20);
            int maxWidth = col == columnCount - 1 ? 1400 : 420;
            width = Math.max(minWidth, Math.min(width, maxWidth));
            widths[col] = width;
            mins[col] = minWidth;
            totalWidth += width;
        }

        int availableWidth = resolveAvailableTableWidth(table);
        if (availableWidth > totalWidth) {
            int extra = availableWidth - totalWidth;
            int textColumns = Math.max(1, columnCount - 1);
            for (int col = 0; col < columnCount; col++) {
                int share = (col == columnCount - 1)
                        ? Math.max(extra / 3, extra / columnCount)
                        : Math.max(0, (extra - Math.max(extra / 3, extra / columnCount)) / textColumns);
                widths[col] += share;
            }
            int adjustedTotal = 0;
            for (int width : widths) adjustedTotal += width;
            widths[columnCount - 1] += Math.max(0, availableWidth - adjustedTotal);
        }

        for (int col = 0; col < columnCount; col++) {
            TableColumn column = table.getColumnModel().getColumn(col);
            column.setMinWidth(mins[col]);
            column.setPreferredWidth(widths[col]);
        }
    }

    private static int resolveAvailableTableWidth(JTable table) {
        if (table == null) return 0;
        java.awt.Container parent = table.getParent();
        if (parent instanceof JViewport) {
            int width = ((JViewport) parent).getExtentSize().width;
            if (width > 0) return Math.max(0, width - 8);
        }
        return Math.max(0, table.getWidth() - 8);
    }

    private static int measureHeaderWidth(JTable table, int columnIndex) {
        JTableHeader header = table.getTableHeader();
        if (header == null) return 80;
        TableCellRenderer renderer = header.getDefaultRenderer();
        Object value = table.getColumnModel().getColumn(columnIndex).getHeaderValue();
        Component component = renderer.getTableCellRendererComponent(table, value, false, false, -1, columnIndex);
        return component == null ? 80 : component.getPreferredSize().width;
    }

    private static int measureCellWidth(JTable table, int row, int column) {
        TableCellRenderer renderer = table.getCellRenderer(row, column);
        Object value = table.getValueAt(row, column);
        Component component = renderer.getTableCellRendererComponent(table, value, false, false, row, column);
        return component == null ? 80 : component.getPreferredSize().width;
    }
    public static void styleScrollPane(JScrollPane pane) {
        if (pane == null) return;
        pane.setBorder(BorderFactory.createEmptyBorder());
        pane.setBackground(CARD_BG);
        pane.setOpaque(true);
        styleScrollBar(pane.getVerticalScrollBar());
        styleScrollBar(pane.getHorizontalScrollBar());
        if (pane.getViewport() != null) {
            pane.getViewport().setBackground(CARD_BG);
            pane.getViewport().setOpaque(true);
        }
        if (pane.getColumnHeader() != null) {
            pane.getColumnHeader().setOpaque(false);
            pane.getColumnHeader().setBackground(CARD_BG);
        }
    }

    public static void styleScrollBar(JScrollBar bar) {
        if (bar == null) return;
        bar.setOpaque(false);
        bar.setUnitIncrement(16);
        bar.setBlockIncrement(96);
        int thickness = 12;
        if (bar.getOrientation() == JScrollBar.VERTICAL) {
            bar.setPreferredSize(new Dimension(thickness, Math.max(thickness, bar.getPreferredSize().height)));
        } else {
            bar.setPreferredSize(new Dimension(Math.max(thickness, bar.getPreferredSize().width), thickness));
        }
        bar.setBorder(BorderFactory.createEmptyBorder());
        bar.setUI(new ModernScrollBarUI());
    }
    public static void styleTextField(JTextField field) {
        if (field == null) return;
        field.setBackground(CARD_BG);
        field.setForeground(TEXT);
        field.setCaretColor(TEXT);
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(7, 11, 7, 11)));
        field.setPreferredSize(new Dimension(field.getPreferredSize().width, 36));
    }

    public static void styleTextArea(JTextArea area) {
        if (area == null) return;
        area.setBackground(CARD_BG);
        area.setForeground(TEXT);
        area.setCaretColor(TEXT);
        area.setBorder(BorderFactory.createEmptyBorder(8, 10, 8, 10));
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
    }

    public static void styleCombo(JComboBox<?> combo) {
        if (combo == null) return;
        combo.setBackground(CARD_BG);
        combo.setForeground(TEXT);
        combo.setFont(combo.getFont().deriveFont(Font.PLAIN, 13f));
        combo.setMaximumRowCount(8);
        combo.setPreferredSize(new Dimension(Math.max(120, combo.getPreferredSize().width), 36));
        combo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                BorderFactory.createEmptyBorder(0, 6, 0, 6)));
        combo.setRenderer(new ModernComboRenderer());
        combo.setUI(new ModernComboBoxUI());
    }

    public static void styleButton(JButton button) {
        styleButton(button, false, false);
    }

    public static void stylePrimaryButton(JButton button) {
        styleButton(button, true, false);
    }

    public static void styleDangerButton(JButton button) {
        styleButton(button, false, true);
    }

    private static void styleButton(JButton button, boolean primary, boolean danger) {
        if (button == null) return;
        button.putClientProperty("uiTheme.primary", Boolean.valueOf(primary));
        button.putClientProperty("uiTheme.danger", Boolean.valueOf(danger));
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setRolloverEnabled(true);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        Color accent = danger ? DANGER : actionAccent(button.getText());
        button.putClientProperty("uiTheme.accent", accent);
        Color fg = primary ? Color.WHITE : accent;
        if (button.getIcon() == null) {
            ImageIcon icon = iconForAction(button.getText(), fg);
            if (icon != null) {
                button.setIcon(icon);
                button.setIconTextGap(7);
            }
        }
        button.setFont(button.getFont().deriveFont(primary ? Font.BOLD : Font.PLAIN, 12.5f));
        button.setMargin(new Insets(8, 14, 8, 14));
        button.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBackground(primary ? accent : tint(accent, danger ? 0.94f : 0.93f));
        button.setForeground(primary ? Color.WHITE : accent);
        button.setUI(new ModernButtonUI());
    }

    public static void styleTree(Component component) {
        if (component == null) return;
        if (component instanceof JPanel) {
            JPanel panel = (JPanel) component;
            if (panel.getBorder() instanceof javax.swing.border.TitledBorder) {
                String title = ((javax.swing.border.TitledBorder) panel.getBorder()).getTitle();
                stylePanelCard(panel, title);
            } else {
                panel.setOpaque(false);
            }
        } else if (component instanceof JTable) {
            styleTable((JTable) component);
        } else if (component instanceof JScrollPane) {
            styleScrollPane((JScrollPane) component);
        } else if (component instanceof JTextArea) {
            styleTextArea((JTextArea) component);
        } else if (component instanceof JTextField) {
            styleTextField((JTextField) component);
        } else if (component instanceof JComboBox) {
            styleCombo((JComboBox<?>) component);
        } else if (component instanceof JCheckBox) {
            JCheckBox box = (JCheckBox) component;
            box.setOpaque(false);
            box.setForeground(TEXT);
            box.setFocusPainted(false);
            box.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        } else if (component instanceof JEditorPane) {
            JEditorPane editor = (JEditorPane) component;
            editor.setBackground(CARD_BG);
            editor.setForeground(TEXT);
            editor.setBorder(BorderFactory.createEmptyBorder(10, 12, 10, 12));
        } else if (component instanceof JButton) {
            JButton button = (JButton) component;
            String t = button.getText() == null ? "" : button.getText().toLowerCase();
            if (t.contains("delete") || t.contains("reject") || t.contains("clear") || t.contains("close")) {
                styleDangerButton(button);
            } else if (t.contains("save") || t.contains("submit") || t.contains("apply") || t.contains("accept") || t.contains("enable")) {
                stylePrimaryButton(button);
            } else {
                styleButton(button);
            }
        } else if (component instanceof JLabel) {
            component.setForeground(TEXT);
        }

        if (component instanceof java.awt.Container) {
            Component[] children = ((java.awt.Container) component).getComponents();
            for (int i = 0; i < children.length; i++) {
                styleTree(children[i]);
            }
        }
    }

    public static Color accentFor(String key) {
        if (key == null || key.trim().isEmpty()) return PRIMARY;
        int hash = Math.abs(key.toLowerCase().hashCode());
        return ACCENTS[hash % ACCENTS.length];
    }

    public static Color tint(Color c, float factor) {
        int r = (int) (255 - (255 - c.getRed()) * (1f - factor));
        int g = (int) (255 - (255 - c.getGreen()) * (1f - factor));
        int b = (int) (255 - (255 - c.getBlue()) * (1f - factor));
        return new Color(Math.min(255, r), Math.min(255, g), Math.min(255, b));
    }

    public static JPanel iconBadge(ImageIcon icon, Color accent) {
        RoundedPanel badge = new RoundedPanel(16);
        badge.setBackground(tint(accent == null ? PRIMARY : accent, 0.88f));
        badge.setLayout(new java.awt.GridBagLayout());
        badge.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        badge.add(new JLabel(icon));
        badge.setPreferredSize(new Dimension(42, 42));
        badge.setMinimumSize(new Dimension(42, 42));
        return badge;
    }

    private static Color actionAccent(String text) {
        String t = text == null ? "" : text.toLowerCase();
        if (t.contains("delete") || t.contains("reject") || t.contains("clear") || t.contains("close")) return DANGER;
        if (t.contains("save") || t.contains("submit") || t.contains("apply") || t.contains("accept") || t.contains("enable")) return SUCCESS;
        if (t.contains("export") || t.contains("download")) return PURPLE;
        if (t.contains("ai") || t.contains("polish") || t.contains("preview") || t.contains("details")) return CYAN;
        if (t.contains("browse") || t.contains("open") || t.contains("edit")) return ORANGE;
        return PRIMARY;
    }

    private static ImageIcon iconForAction(String text, Color color) {
        String t = text == null ? "" : text.toLowerCase();
        int size = 16;
        if (t.contains("delete") || t.contains("clear")) return IconFactory.menu(size, color);
        if (t.contains("reject") || t.contains("disable")) return IconFactory.shield(size, color);
        if (t.contains("save") || t.contains("submit") || t.contains("apply") || t.contains("accept") || t.contains("enable")) return IconFactory.check(size, color);
        if (t.contains("export") || t.contains("download")) return IconFactory.download(size, color);
        if (t.contains("refresh") || t.contains("reload")) return IconFactory.gear(size, color);
        if (t.contains("browse") || t.contains("open") || t.contains("details") || t.contains("preview")) return IconFactory.document(size, color);
        if (t.contains("edit") || t.contains("category")) return IconFactory.hammer(size, color);
        if (t.contains("send")) return IconFactory.envelope(size, color);
        return null;
    }

    private static final class SectionBorder implements Border {
        private final String title;
        private final Color accent;
        private final Insets insets = new Insets(54, 18, 16, 18);

        private SectionBorder(String title, Color accent) {
            this.title = title == null ? "" : title;
            this.accent = accent == null ? PRIMARY : accent;
        }

        public Insets getBorderInsets(Component c) {
            return insets;
        }

        public boolean isBorderOpaque() {
            return false;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 14));
                g2.fillRoundRect(x + 5, y + 7, Math.max(0, width - 10), Math.max(0, height - 10), 26, 26);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(x, y, Math.max(0, width - 1), Math.max(0, height - 1), 26, 26);
                g2.setColor(tint(accent, 0.88f));
                g2.fillRoundRect(x + 16, y + 12, 28, 28, 14, 14);
                g2.setColor(accent);
                g2.fillOval(x + 27, y + 23, 7, 7);
                g2.setPaint(new GradientPaint(x + 54, y, accent, x + Math.max(55, width - 18), y,
                        new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 170)));
                g2.fillRoundRect(x + 54, y + 16, Math.max(24, width - 82), 4, 6, 6);
                g2.setColor(new Color(148, 163, 184, 90));
                g2.drawRoundRect(x, y, Math.max(0, width - 1), Math.max(0, height - 1), 26, 26);
                if (!title.isEmpty()) {
                    Font base = c.getFont() == null ? new Font(Font.SANS_SERIF, Font.BOLD, 13) : c.getFont();
                    g2.setFont(base.deriveFont(Font.BOLD, 14f));
                    FontMetrics fm = g2.getFontMetrics();
                    g2.setColor(TEXT);
                    g2.drawString(title, x + 54, y + 38 + (fm.getAscent() - fm.getDescent()) / 12);
                }
            } finally {
                g2.dispose();
            }
        }
    }

    public static class RoundedPanel extends JPanel {
        private final int arc;

        public RoundedPanel(int arc) {
            this.arc = arc;
            setOpaque(false);
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(15, 23, 42, 12));
                g2.fillRoundRect(3, 5, Math.max(0, getWidth() - 6), Math.max(0, getHeight() - 7), arc, arc);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);
                g2.setColor(new Color(148, 163, 184, 55));
                g2.drawRoundRect(0, 0, Math.max(0, getWidth() - 1), Math.max(0, getHeight() - 1), arc, arc);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }


    private static final class ModernComboRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                boolean isSelected, boolean cellHasFocus) {
            JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            label.setBorder(BorderFactory.createEmptyBorder(7, 10, 7, 10));
            label.setFont(label.getFont().deriveFont(Font.PLAIN, 13f));
            label.setText(value == null ? "" : String.valueOf(value));
            if (isSelected) {
                label.setBackground(new Color(232, 242, 255));
                label.setForeground(TEXT);
            } else {
                label.setBackground(CARD_BG);
                label.setForeground(TEXT);
            }
            return label;
        }
    }

    private static final class ModernComboBoxUI extends BasicComboBoxUI {
        @Override
        protected JButton createArrowButton() {
            JButton button = new JButton() {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    try {
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2.setColor(new Color(241, 245, 249));
                        g2.fillRoundRect(5, 6, Math.max(0, getWidth() - 10), Math.max(0, getHeight() - 12), 12, 12);
                        g2.setColor(new Color(100, 116, 139));
                        int cx = getWidth() / 2;
                        int cy = getHeight() / 2 + 1;
                        int s = 4;
                        g2.drawLine(cx - s, cy - 2, cx, cy + 2);
                        g2.drawLine(cx, cy + 2, cx + s, cy - 2);
                    } finally {
                        g2.dispose();
                    }
                }
            };
            button.setBorder(BorderFactory.createEmptyBorder());
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setPreferredSize(new Dimension(24, 24));
            return button;
        }

        @Override
        protected void installDefaults() {
            super.installDefaults();
            comboBox.setOpaque(false);
        }

        @Override
        protected ComboPopup createPopup() {
            BasicComboPopup popup = new BasicComboPopup(comboBox) {
                @Override
                protected JScrollPane createScroller() {
                    JScrollPane scrollPane = super.createScroller();
                    scrollPane.setBorder(BorderFactory.createEmptyBorder());
                    if (scrollPane.getViewport() != null) {
                        scrollPane.getViewport().setBackground(CARD_BG);
                    }
                    UiTheme.styleScrollBar(scrollPane.getVerticalScrollBar());
                    UiTheme.styleScrollBar(scrollPane.getHorizontalScrollBar());
                    return scrollPane;
                }
            };
            popup.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(226, 232, 240), 1),
                    BorderFactory.createEmptyBorder(4, 4, 4, 4)));
            popup.setBackground(CARD_BG);
            popup.getList().setBackground(CARD_BG);
            popup.getList().setForeground(TEXT);
            popup.getList().setSelectionBackground(new Color(232, 242, 255));
            popup.getList().setSelectionForeground(TEXT);
            popup.getList().setFixedCellHeight(34);
            popup.getList().setBorder(BorderFactory.createEmptyBorder());
            return popup;
        }

        @Override
        public void paintCurrentValueBackground(Graphics g, java.awt.Rectangle bounds, boolean hasFocus) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, 14, 14);
            } finally {
                g2.dispose();
            }
        }
    }
    private static final class ModernButtonUI extends BasicButtonUI {
        public Dimension getPreferredSize(JComponent c) {
            Dimension d = super.getPreferredSize(c);
            if (d == null) return new Dimension(96, 38);
            return new Dimension(Math.max(d.width + 4, 88), Math.max(d.height, 38));
        }

        public void paint(Graphics g, JComponent c) {
            AbstractButton b = (AbstractButton) c;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                ButtonModel model = b.getModel();
                boolean primary = Boolean.TRUE.equals(b.getClientProperty("uiTheme.primary"));
                boolean danger = Boolean.TRUE.equals(b.getClientProperty("uiTheme.danger"));
                Color accent = (Color) b.getClientProperty("uiTheme.accent");
                if (accent == null) accent = primary ? PRIMARY : (danger ? DANGER : PRIMARY);
                Color fill;
                if (!b.isEnabled()) {
                    fill = new Color(226, 232, 240);
                    b.setForeground(new Color(148, 163, 184));
                } else if (primary) {
                    fill = model.isPressed() ? accent.darker() : (model.isRollover() ? brighten(accent, 1.06f) : accent);
                } else {
                    fill = model.isPressed() ? tint(accent, 0.84f) : (model.isRollover() ? tint(accent, 0.88f) : tint(accent, 0.93f));
                }
                if (model.isRollover() && b.isEnabled()) {
                    g2.setColor(new Color(15, 23, 42, 14));
                    g2.fillRoundRect(2, 3, Math.max(0, c.getWidth() - 4), Math.max(0, c.getHeight() - 4), 18, 18);
                }
                g2.setColor(fill);
                g2.fillRoundRect(0, 0, Math.max(0, c.getWidth() - 1), Math.max(0, c.getHeight() - 1), 18, 18);
            } finally {
                g2.dispose();
            }
            super.paint(g, c);
        }
    }

    private static final class ZebraRenderer extends DefaultTableCellRenderer {
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground((row % 2 == 0) ? CARD_BG : new Color(250, 253, 255));
                c.setForeground(TEXT);
            } else {
                c.setBackground(new Color(232, 242, 255));
                c.setForeground(TEXT);
            }
            setFont(table.getFont().deriveFont(Font.PLAIN, 13f));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240, 150)),
                    BorderFactory.createEmptyBorder(0, 12, 0, 12)));
            setToolTipText(value == null ? null : String.valueOf(value));
            return c;
        }
    }

    private static final class ModernHeaderRenderer extends DefaultTableCellRenderer {
        private int column;
        private boolean lastColumn;

        private ModernHeaderRenderer() {
            setHorizontalAlignment(SwingConstants.LEFT);
            setOpaque(false);
        }

        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            this.column = column;
            this.lastColumn = column == table.getColumnCount() - 1;
            setText(value == null ? "" : String.valueOf(value));
            setForeground(new Color(37, 58, 92));
            setFont(table.getTableHeader().getFont().deriveFont(Font.BOLD, 13f));
            setBorder(BorderFactory.createEmptyBorder(0, 14, 0, 14));
            return this;
        }

        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int arc = 18;
                g2.setColor(new Color(245, 249, 255));
                if (column == 0 && lastColumn) {
                    g2.fillRoundRect(0, 2, getWidth(), getHeight() - 6, arc, arc);
                } else if (column == 0) {
                    g2.fillRoundRect(0, 2, getWidth() + arc, getHeight() - 6, arc, arc);
                } else if (lastColumn) {
                    g2.fillRoundRect(-arc, 2, getWidth() + arc, getHeight() - 6, arc, arc);
                } else {
                    g2.fillRect(0, 2, getWidth(), getHeight() - 6);
                }
                g2.setColor(new Color(37, 99, 235, 170));
                g2.fillRoundRect(12, getHeight() - 7, Math.max(16, getWidth() - 24), 3, 5, 5);
            } finally {
                g2.dispose();
            }
            super.paintComponent(g);
        }
    }

    private static final class ModernTableHeaderUI extends BasicTableHeaderUI {
        public void paint(Graphics g, JComponent c) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setColor(CARD_BG);
                g2.fillRect(0, 0, c.getWidth(), c.getHeight());
            } finally {
                g2.dispose();
            }
            super.paint(g, c);
        }
    }

    private static final class ModernScrollBarUI extends BasicScrollBarUI {
        protected void configureScrollBarColors() {
            this.thumbColor = new Color(148, 163, 184, 118);
            this.trackColor = new Color(248, 250, 252, 0);
        }

        protected JButton createDecreaseButton(int orientation) {
            return createZeroButton();
        }

        protected JButton createIncreaseButton(int orientation) {
            return createZeroButton();
        }

        protected void paintTrack(Graphics g, JComponent c, java.awt.Rectangle trackBounds) {
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(226, 232, 240, 70));
                if (scrollbar.getOrientation() == JScrollBar.VERTICAL) {
                    int w = 4;
                    int x = trackBounds.x + (trackBounds.width - w) / 2;
                    g2.fillRoundRect(x, trackBounds.y + 4, w, Math.max(0, trackBounds.height - 8), w, w);
                } else {
                    int h = 4;
                    int y = trackBounds.y + (trackBounds.height - h) / 2;
                    g2.fillRoundRect(trackBounds.x + 4, y, Math.max(0, trackBounds.width - 8), h, h, h);
                }
            } finally {
                g2.dispose();
            }
        }

        protected void paintThumb(Graphics g, JComponent c, java.awt.Rectangle thumbBounds) {
            if (thumbBounds.isEmpty() || !scrollbar.isEnabled()) return;
            Graphics2D g2 = (Graphics2D) g.create();
            try {
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean vertical = scrollbar.getOrientation() == JScrollBar.VERTICAL;
                boolean dragging = isDragging;
                boolean rollover = isThumbRollover();
                Color fill = dragging ? new Color(37, 99, 235, 190)
                        : (rollover ? new Color(37, 99, 235, 150) : new Color(148, 163, 184, 116));
                g2.setColor(fill);
                if (vertical) {
                    int w = 8;
                    int x = thumbBounds.x + (thumbBounds.width - w) / 2;
                    g2.fillRoundRect(x, thumbBounds.y + 3, w, Math.max(18, thumbBounds.height - 6), 8, 8);
                } else {
                    int h = 8;
                    int y = thumbBounds.y + (thumbBounds.height - h) / 2;
                    g2.fillRoundRect(thumbBounds.x + 3, y, Math.max(18, thumbBounds.width - 6), h, 8, 8);
                }
            } finally {
                g2.dispose();
            }
        }

        protected Dimension getMinimumThumbSize() {
            return new Dimension(22, 22);
        }

        private JButton createZeroButton() {
            JButton button = new JButton();
            button.setPreferredSize(new Dimension(0, 0));
            button.setMinimumSize(new Dimension(0, 0));
            button.setMaximumSize(new Dimension(0, 0));
            button.setOpaque(false);
            button.setFocusable(false);
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            return button;
        }
    }

    private static Color brighten(Color c, float factor) {
        return new Color(Math.min(255, (int) (c.getRed() * factor)),
                Math.min(255, (int) (c.getGreen() * factor)),
                Math.min(255, (int) (c.getBlue() * factor)));
    }
}
