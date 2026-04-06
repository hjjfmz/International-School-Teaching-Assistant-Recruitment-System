package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ImageIcon;

public final class StartPanel extends JPanel {
    public interface StartHandler {
        void onStart();
    }

    public StartPanel(StartHandler handler) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JPanel center = new JPanel(new BorderLayout(12, 12));

        JLabel title = new JLabel(I18n.t("app.title"), SwingConstants.CENTER);
        title.setFont(title.getFont().deriveFont(22f));

        JLabel subtitle = new JLabel(I18n.t("start.subtitle"), SwingConstants.CENTER);

        JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
        BufferedImage[] original = new BufferedImage[1];
        try {
            File imgFile = new File(System.getProperty("user.dir"), "1.jpg");
            if (imgFile.isFile()) {
                original[0] = ImageIO.read(imgFile);
            }
        } catch (IOException ignored) {
        }

        Runnable refreshImage = () -> {
            if (original[0] == null) {
                imageLabel.setIcon(null);
                return;
            }
            int availableW = Math.max(1, center.getWidth() - 40);
            int availableH = Math.max(1, center.getHeight() - 180);
            int maxW = Math.min(availableW, 900);
            int maxH = Math.min(availableH, 420);

            int ow = original[0].getWidth();
            int oh = original[0].getHeight();
            if (ow <= 0 || oh <= 0) return;

            double scale = Math.min((double) maxW / (double) ow, (double) maxH / (double) oh);
            scale = Math.max(0.05, Math.min(1.0, scale));
            int w = Math.max(1, (int) Math.round(ow * scale));
            int h = Math.max(1, (int) Math.round(oh * scale));
            Image scaled = original[0].getScaledInstance(w, h, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaled));
        };

        JButton startBtn = new JButton(I18n.t("start.button"));
        startBtn.setPreferredSize(new Dimension(180, 40));
        startBtn.addActionListener(e -> {
            if (handler != null) handler.onStart();
        });

        JComboBox<I18n.Lang> lang = new JComboBox<I18n.Lang>(I18n.Lang.values());
        lang.setSelectedItem(I18n.Lang.EN);
        lang.setEnabled(false);

        JPanel bottom = new JPanel();
        bottom.add(lang);
        bottom.add(startBtn);

        center.add(title, BorderLayout.NORTH);
        JPanel middle = new JPanel(new BorderLayout(8, 8));
        middle.add(imageLabel, BorderLayout.CENTER);
        middle.add(subtitle, BorderLayout.SOUTH);
        center.add(middle, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);

        center.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshImage.run();
            }
        });
        SwingUtilities.invokeLater(refreshImage);
    }
}
