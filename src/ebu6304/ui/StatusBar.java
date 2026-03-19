package ebu6304.ui;

import java.awt.BorderLayout;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

public final class StatusBar extends JPanel {
    private final JLabel left = new JLabel(" ");
    private final JLabel right = new JLabel(" ");

    public StatusBar() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(left, BorderLayout.WEST);
        add(right, BorderLayout.EAST);
    }

    public void setLeftText(String t) {
        left.setText(t == null ? " " : t);
    }

    public void setRightText(String t) {
        right.setText(t == null ? " " : t);
    }
}
