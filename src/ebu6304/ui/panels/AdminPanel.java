package ebu6304.ui.panels;

import ebu6304.storage.DataService;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public final class AdminPanel extends JPanel {
    public AdminPanel(DataService data) {
        setLayout(new BorderLayout());
        add(new JLabel("This panel has been retired. Please use the new Workbench Admin pages."), BorderLayout.CENTER);
    }
}
