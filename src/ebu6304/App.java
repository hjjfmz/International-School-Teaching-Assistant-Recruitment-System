package ebu6304;

import java.awt.Font;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ebu6304.storage.DataService;
import ebu6304.ui.MainFrame;

public final class App {
    public static void main(String[] args) {
        resolveProjectRoot();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setLookAndFeel();
                DataService data = new DataService();
                data.init();
                MainFrame frame = new MainFrame(data);
                frame.setVisible(true);
            }
        });
    }

    private static java.io.File root;

    public static java.io.File projectRoot() {
        return root;
    }

    private static void resolveProjectRoot() {
        java.io.File cwd = new java.io.File(System.getProperty("user.dir"));
        if (hasMarker(cwd)) { root = cwd; return; }

        try {
            java.security.CodeSource cs = App.class.getProtectionDomain().getCodeSource();
            if (cs != null && cs.getLocation() != null) {
                java.io.File classRoot = new java.io.File(cs.getLocation().toURI());
                if (classRoot.isDirectory()) {
                    if (hasMarker(classRoot)) { root = classRoot; return; }
                    java.io.File parent = classRoot.getParentFile();
                    if (parent != null && hasMarker(parent)) { root = parent; return; }
                } else {
                    java.io.File parent = classRoot.getParentFile();
                    if (parent != null && hasMarker(parent)) { root = parent; return; }
                }
            }
        } catch (Exception ignored) {}

        for (java.io.File dir = cwd; dir != null; dir = dir.getParentFile()) {
            if (hasMarker(dir)) { root = dir; return; }
        }
        root = cwd;
    }

    private static boolean hasMarker(java.io.File dir) {
        return new java.io.File(dir, "images").isDirectory()
            || new java.io.File(dir, "data").isDirectory();
    }

    private static void setLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            Font font = pickFont(Font.PLAIN, 13);
            Font bold = font.deriveFont(Font.BOLD);

            String[] keys = {
                "Label.font", "Button.font", "TextField.font",
                "TextArea.font", "List.font", "ComboBox.font",
                "TabbedPane.font", "Table.font", "TableHeader.font",
                "Tree.font", "Menu.font", "MenuItem.font",
                "CheckBox.font", "RadioButton.font",
                "ToolTip.font", "Spinner.font", "Panel.font",
                "OptionPane.messageFont", "OptionPane.buttonFont",
                "PasswordField.font", "EditorPane.font",
                "TextPane.font", "ProgressBar.font",
                "PopupMenu.font", "CheckBoxMenuItem.font",
                "RadioButtonMenuItem.font", "FormattedTextField.font",
                "ScrollPane.font", "Viewport.font",
            };
            for (String k : keys) {
                UIManager.put(k, font);
            }
            UIManager.put("TitledBorder.font", bold);

            UIManager.put("OptionPane.yesButtonText", "Yes");
            UIManager.put("OptionPane.noButtonText", "No");
            UIManager.put("OptionPane.cancelButtonText", "Cancel");
            UIManager.put("OptionPane.okButtonText", "OK");

            UIManager.put("FileChooser.openButtonText", "Open");
            UIManager.put("FileChooser.cancelButtonText", "Cancel");
            UIManager.put("FileChooser.saveButtonText", "Save");
            UIManager.put("FileChooser.lookInLabelText", "Look in:");
            UIManager.put("FileChooser.fileNameLabelText", "File name:");
            UIManager.put("FileChooser.filesOfTypeLabelText", "Files of type:");
        } catch (Exception ignored) {
        }
    }

    private static Font pickFont(int style, float size) {
        String[] candidates = {
            "Segoe UI",
            "Microsoft YaHei UI",
            "Microsoft YaHei",
            "Noto Sans CJK SC",
            "Noto Sans",
            "Ubuntu",
            "DejaVu Sans",
        };
        java.awt.GraphicsEnvironment ge = java.awt.GraphicsEnvironment.getLocalGraphicsEnvironment();
        java.util.Set<String> available = new java.util.HashSet<String>(
                java.util.Arrays.asList(ge.getAvailableFontFamilyNames()));
        for (String name : candidates) {
            if (available.contains(name)) {
                Font f = new Font(name, style, (int) size);
                if (f.canDisplay('A') && f.canDisplay('\u4e2d')) {
                    return f;
                }
            }
        }
        return new Font(Font.SANS_SERIF, style, (int) size);
    }
}
