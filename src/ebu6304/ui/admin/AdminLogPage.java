package ebu6304.ui.admin;

import java.awt.BorderLayout;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import ebu6304.storage.DataService;

public final class AdminLogPage extends JPanel {
    private final DataService data;
    private final JTextArea area = new JTextArea();

    public AdminLogPage(DataService data) {
        super(new BorderLayout(10, 10));
        this.data = data;
        setBorder(BorderFactory.createTitledBorder("Operation Logs"));

        area.setEditable(false);
        JButton refresh = new JButton("Refresh");
        refresh.addActionListener(e -> refresh());

        add(refresh, BorderLayout.NORTH);
        add(new JScrollPane(area), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        try {
            List<String> lines = Files.readAllLines(data.dataDir().resolve("temp_operation.txt"), StandardCharsets.UTF_8);
            StringBuilder sb = new StringBuilder();
            for (String l : lines) sb.append(l).append("\n");
            area.setText(sb.toString());
        } catch (IOException e) {
            area.setText("" );
        }
    }
}
