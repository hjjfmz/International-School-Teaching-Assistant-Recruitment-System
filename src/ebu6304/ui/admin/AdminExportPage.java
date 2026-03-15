package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import ebu6304.storage.DataService;

public final class AdminExportPage extends JPanel {
    private final DataService data;
    private final JComboBox<String> type = new JComboBox<String>(new String[] { "TA Info", "MO Jobs", "All" });

    public AdminExportPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Data Export"));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.add(new JLabel("Type:"));
        left.add(type);
        top.add(left, BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton export = new JButton("Export" );
        actions.add(export);
        top.add(actions, BorderLayout.EAST);

        export.addActionListener(e -> export());

        add(top, BorderLayout.NORTH);
        add(new JLabel("Mid-fidelity: exporting will copy data files to the selected folder"), BorderLayout.CENTER);
    }

    private void export() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;

        java.nio.file.Path dir = chooser.getSelectedFile().toPath();
        String t = String.valueOf(type.getSelectedItem());
        try {
            if ("TA Info".equals(t)) {
                Files.copy(data.dataDir().resolve("ta_info.csv"), dir.resolve("ta_info.csv"), StandardCopyOption.REPLACE_EXISTING);
            } else if ("MO Jobs".equals(t)) {
                Files.copy(data.dataDir().resolve("mo_jobs.json"), dir.resolve("mo_jobs.json"), StandardCopyOption.REPLACE_EXISTING);
            } else {
                Files.copy(data.dataDir().resolve("ta_info.csv"), dir.resolve("ta_info.csv"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(data.dataDir().resolve("mo_jobs.json"), dir.resolve("mo_jobs.json"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(data.dataDir().resolve("admin_system.xml"), dir.resolve("admin_system.xml"), StandardCopyOption.REPLACE_EXISTING);
                Files.copy(data.dataDir().resolve("temp_operation.txt"), dir.resolve("temp_operation.txt"), StandardCopyOption.REPLACE_EXISTING);
            }
            JOptionPane.showMessageDialog(this, "Exported" );
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Export failed" );
        }
    }
}
