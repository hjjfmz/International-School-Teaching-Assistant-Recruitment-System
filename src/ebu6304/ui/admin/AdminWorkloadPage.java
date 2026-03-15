package ebu6304.ui.admin;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class AdminWorkloadPage extends JPanel {
    public AdminWorkloadPage() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        add(new JLabel("TA Workload (placeholder; AI features planned for Sprint2)"), BorderLayout.NORTH);
    }
}
