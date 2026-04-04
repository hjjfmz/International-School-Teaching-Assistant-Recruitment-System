package ebu6304.ui.ta;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;

public final class TaProfilePage extends JPanel {

    private static final String CARD_HUB    = "hub";
    private static final String CARD_LIST   = "list";
    private static final String CARD_CREATE = "create";
    private static final String CARD_EDIT   = "edit";

    private final DataService data;
    private final String account;

    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);

    // ---- List card ----
    private final DefaultTableModel listModel;
    private final JTable listTable;

    // ---- Create card fields ----
    private final JTextField c_idField     = new JTextField(20);
    private final JTextField c_nameField   = new JTextField(20);
    private final JTextField c_emailField  = new JTextField(20);
    private final JTextField c_skillsField = new JTextField(20);
    private final JTextField c_cvField     = new JTextField(20);
    private final JTextArea  c_descArea    = new JTextArea(4, 20);

    // ---- Edit card fields ----
    private final JTextField e_idField     = new JTextField(20);
    private final JTextField e_nameField   = new JTextField(20);
    private final JTextField e_emailField  = new JTextField(20);
    private final JTextField e_skillsField = new JTextField(20);
    private final JTextField e_cvField     = new JTextField(20);
    private final JTextArea  e_descArea    = new JTextArea(4, 20);
    private final JButton    e_deleteBtn   = new JButton("Delete Profile");

    public TaProfilePage(DataService data, String account) {
        super(new BorderLayout());
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        listModel = new DefaultTableModel(
                new Object[]{"Account (ID)", "Name", "Email", "Skills", "CV Path"}, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        listTable = new JTable(listModel);
        listTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2 && listTable.getSelectedRow() >= 0) {
                    showEdit();
                }
            }
        });

        deck.add(buildHubCard(),    CARD_HUB);
        deck.add(buildListCard(),   CARD_LIST);
        deck.add(buildCreateCard(), CARD_CREATE);
        deck.add(buildEditCard(),   CARD_EDIT);

        add(deck, BorderLayout.CENTER);
        showHub();
    }

    // ------------------------------------------------------------------ hub --
    private JPanel buildHubCard() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setBorder(BorderFactory.createTitledBorder("My Profile"));

        JLabel hint = new JLabel("What would you like to do?", SwingConstants.CENTER);
        hint.setFont(hint.getFont().deriveFont(Font.PLAIN, 14f));

        JButton createBtn = new JButton("Create a Profile");
        JButton manageBtn = new JButton("Manage My Profile");
        createBtn.setPreferredSize(new Dimension(200, 40));
        manageBtn.setPreferredSize(new Dimension(200, 40));

        createBtn.addActionListener(e -> showCreate());
        manageBtn.addActionListener(e -> {
            Applicant a = data.getApplicant(account).orElse(null);
            if (a == null || (a.email().isEmpty() && a.skills().isEmpty() && a.description().isEmpty())) {
                JOptionPane.showMessageDialog(this,
                        "No profile found. Please create a profile first.",
                        "No Profile", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            showList();
        });

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 10, 12, 10);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(hint, c);
        c.gridy = 1; c.gridwidth = 1; p.add(createBtn, c);
        c.gridx = 1; p.add(manageBtn, c);
        return p;
    }

    // ----------------------------------------------------------------- list --
    private JPanel buildListCard() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("My Profile"));

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Select your profile to view or edit"), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back   = new JButton("< Back");
        JButton edit   = new JButton("Edit");
        JButton delete = new JButton("Delete");
        delete.setForeground(Color.RED);
        actions.add(back);
        actions.add(delete);
        actions.add(edit);
        top.add(actions, BorderLayout.EAST);

        back.addActionListener(e -> showHub());
        edit.addActionListener(e -> {
            if (listTable.getSelectedRow() < 0) {
                JOptionPane.showMessageDialog(this, "Please select a profile row first.");
                return;
            }
            showEdit();
        });
        delete.addActionListener(e -> {
            if (listTable.getSelectedRow() < 0) {
                JOptionPane.showMessageDialog(this, "Please select a profile row first.");
                return;
            }
            deleteProfile();
        });

        p.add(top, BorderLayout.NORTH);
        p.add(new JScrollPane(listTable), BorderLayout.CENTER);
        return p;
    }

    // --------------------------------------------------------------- create --
    private JPanel buildCreateCard() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Create a Profile"));

        c_idField.setEditable(false);
        c_nameField.setEditable(false);
        c_cvField.setEditable(false);
        c_descArea.setLineWrap(true);
        c_descArea.setWrapStyleWord(true);

        JButton browseCv = new JButton("Browse...");
        browseCv.addActionListener(e -> browseAndSetCv(c_cvField));

        JPanel form = buildForm(c_idField, c_nameField, c_emailField, c_skillsField,
                                c_cvField, browseCv, c_descArea);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("< Back");
        JButton save = new JButton("Save Profile");
        back.addActionListener(e -> showHub());
        save.addActionListener(e -> saveCreate());
        btns.add(back);
        btns.add(save);

        p.add(form, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    // ----------------------------------------------------------------- edit --
    private JPanel buildEditCard() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Edit Profile"));

        e_idField.setEditable(false);
        e_nameField.setEditable(false);
        e_cvField.setEditable(false);
        e_descArea.setLineWrap(true);
        e_descArea.setWrapStyleWord(true);

        e_deleteBtn.setForeground(Color.RED);
        e_deleteBtn.addActionListener(e2 -> deleteProfile());

        JButton browseCv = new JButton("Browse...");
        browseCv.addActionListener(e -> browseAndSetCv(e_cvField));

        JPanel form = buildForm(e_idField, e_nameField, e_emailField, e_skillsField,
                                e_cvField, browseCv, e_descArea);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("< Back");
        JButton save = new JButton("Save Changes");
        back.addActionListener(e -> showList());
        save.addActionListener(e -> saveEdit());
        btns.add(back);
        btns.add(e_deleteBtn);
        btns.add(save);

        p.add(form, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    // ---------------------------------------------------------- form builder --
    private static JPanel buildForm(JTextField idF, JTextField nameF,
                                    JTextField emailF, JTextField skillsF,
                                    JTextField cvF, JButton browseCvBtn,
                                    JTextArea descA) {
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0; c.gridy = 0; form.add(new JLabel("Account (Student ID)"), c);
        c.gridx = 1; form.add(idF, c);

        c.gridx = 0; c.gridy = 1; form.add(new JLabel("Name"), c);
        c.gridx = 1; form.add(nameF, c);

        c.gridx = 0; c.gridy = 2; form.add(new JLabel("Email *"), c);
        c.gridx = 1; form.add(emailF, c);

        c.gridx = 0; c.gridy = 3; form.add(new JLabel("Skills"), c);
        c.gridx = 1; form.add(skillsF, c);

        // CV row: text field + browse button side by side
        JPanel cvRow = new JPanel(new BorderLayout(4, 0));
        cvRow.add(cvF, BorderLayout.CENTER);
        cvRow.add(browseCvBtn, BorderLayout.EAST);
        c.gridx = 0; c.gridy = 4; form.add(new JLabel("CV / Resume Path"), c);
        c.gridx = 1; form.add(cvRow, c);

        c.gridx = 0; c.gridy = 5; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.CENTER;
        form.add(new JScrollPane(descA), c);

        return form;
    }

    // ------------------------------------------------------------- navigate --
    private void showHub() {
        cards.show(deck, CARD_HUB);
    }

    private void showList() {
        refreshList();
        cards.show(deck, CARD_LIST);
    }

    private void refreshList() {
        listModel.setRowCount(0);
        Applicant a = data.getApplicant(account).orElse(null);
        if (a != null && !(a.email().isEmpty() && a.skills().isEmpty() && a.description().isEmpty())) {
            listModel.addRow(new Object[]{a.id(), a.name(), a.email(), a.skills(), a.cvPath()});
            listTable.setRowSelectionInterval(0, 0);
        }
    }

    private void showCreate() {
        Applicant a = data.getApplicant(account).orElse(null);
        c_idField.setText(account);
        c_nameField.setText(a != null ? a.name() : "");
        c_emailField.setText("");
        c_skillsField.setText("");
        c_cvField.setText(a != null ? a.cvPath() : "");
        c_descArea.setText("");
        cards.show(deck, CARD_CREATE);
    }

    private void showEdit() {
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        e_idField.setText(a.id());
        e_nameField.setText(a.name());
        e_emailField.setText(a.email());
        e_skillsField.setText(a.skills());
        e_cvField.setText(a.cvPath());
        e_descArea.setText(a.description());
        cards.show(deck, CARD_EDIT);
    }

    // --------------------------------------------------------------- actions --
    private void browseAndSetCv(JTextField target) {
        JFileChooser chooser = new JFileChooser();
        int res = chooser.showOpenDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) return;
        File f = chooser.getSelectedFile();
        if (f == null) return;
        String path = f.getAbsolutePath();
        if (!isSupported(path, data.getConfig().cvFormats())) {
            JOptionPane.showMessageDialog(this,
                    "Unsupported CV format. Allowed: " + data.getConfig().cvFormats());
            return;
        }
        // Store file into project cv folder
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        try {
            String stored = data.storeCv(a.id(), path);
            target.setText(stored);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to save CV into project data folder");
        }
    }

    private void saveCreate() {
        String email  = c_emailField.getText().trim();
        String skills = c_skillsField.getText().trim();
        String cv     = c_cvField.getText().trim();
        String desc   = c_descArea.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        data.upsertApplicant(a.withProfile(a.name(), email, skills, cv, desc));
        JOptionPane.showMessageDialog(this, "Profile created successfully.");
        showHub();
    }

    private void saveEdit() {
        String email  = e_emailField.getText().trim();
        String skills = e_skillsField.getText().trim();
        String cv     = e_cvField.getText().trim();
        String desc   = e_descArea.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        data.upsertApplicant(a.withProfile(a.name(), email, skills, cv, desc));
        JOptionPane.showMessageDialog(this, "Profile updated successfully.");
        showList();
    }

    private void deleteProfile() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete your profile information?\n(Email, skills, CV and description will be cleared.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        data.upsertApplicant(a.withProfile(a.name(), "", "", "", ""));
        JOptionPane.showMessageDialog(this, "Profile information deleted.");
        showHub();
    }

    private static boolean isSupported(String path, String formatsCsv) {
        if (path == null) return false;
        String p = path.toLowerCase();
        int dot = p.lastIndexOf('.');
        if (dot < 0) return false;
        String ext = p.substring(dot + 1);
        if (formatsCsv == null || formatsCsv.trim().isEmpty()) {
            return ext.equals("pdf") || ext.equals("doc") || ext.equals("docx");
        }
        for (String s : formatsCsv.toLowerCase().split(",")) {
            if (ext.equals(s.trim())) return true;
        }
        return false;
    }

    /** Called externally to reload/reset the page. */
    public void load() {
        showHub();
    }
}
