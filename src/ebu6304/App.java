package ebu6304;

import java.awt.Font;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import ebu6304.storage.DataService;
import ebu6304.ui.MainFrame;

public final class App {
    public static void main(String[] args) {
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

    private static void setLookAndFeel() {
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }

            Font font = new Font("Segoe UI", Font.PLAIN, 13);
            UIManager.put("Label.font", font);
            UIManager.put("Button.font", font);
            UIManager.put("TextField.font", font);
            UIManager.put("TextArea.font", font);
            UIManager.put("List.font", font);
            UIManager.put("ComboBox.font", font);
            UIManager.put("TabbedPane.font", font);
            UIManager.put("TitledBorder.font", font.deriveFont(Font.BOLD));
        } catch (Exception ignored) {
        }
    }
}
