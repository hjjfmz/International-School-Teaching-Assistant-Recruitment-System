package ebu6304.ui.admin;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class AdminConfigPage extends JPanel {
    public AdminConfigPage() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(new JLabel("System Config (placeholder: data path / password rules / CV formats / default language)"), BorderLayout.NORTH);
    }
}
