package ebu6304.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

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
        center.add(subtitle, BorderLayout.CENTER);
        center.add(bottom, BorderLayout.SOUTH);

        add(center, BorderLayout.CENTER);
    }
}
