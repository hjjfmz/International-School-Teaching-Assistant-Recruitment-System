package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import ebu6304.model.Applicant;
import ebu6304.storage.DataService;

public final class TaProfilePage extends JPanel {

    private static final String CARD_HUB    = "hub";
    private static final String CARD_CREATE = "create";
    private static final String CARD_MANAGE = "manage";

    private final DataService data;
    private final String account;

    private final CardLayout cards = new CardLayout();
    private final JPanel deck = new JPanel(cards);

    // ---- Create card fields ----
    private final JTextField c_idField     = new JTextField(20);
    private final JTextField c_nameField   = new JTextField(20);
    private final JTextField c_emailField  = new JTextField(20);
    private final JTextField c_skillsField = new JTextField(20);
    private final JTextArea  c_descArea    = new JTextArea(4, 20);

    // ---- Manage card fields ----
    private final JTextField m_idField     = new JTextField(20);
    private final JTextField m_nameField   = new JTextField(20);
    private final JTextField m_emailField  = new JTextField(20);
    private final JTextField m_skillsField = new JTextField(20);
    private final JTextArea  m_descArea    = new JTextArea(4, 20);
    private final JButton    m_editBtn     = new JButton("Edit");
    private final JButton    m_saveBtn     = new JButton("Save Changes");
    private final JButton    m_deleteBtn   = new JButton("Delete Profile");

    public TaProfilePage(DataService data, String account) {
        super(new BorderLayout());
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        deck.add(buildHubCard(),    CARD_HUB);
        deck.add(buildCreateCard(), CARD_CREATE);
        deck.add(buildManageCard(), CARD_MANAGE);

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
            showManage();
        });

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(12, 10, 12, 10);
        c.gridx = 0; c.gridy = 0; c.gridwidth = 2; p.add(hint, c);
        c.gridy = 1; c.gridwidth = 1; p.add(createBtn, c);
        c.gridx = 1; p.add(manageBtn, c);
        return p;
    }

    // --------------------------------------------------------------- create --
    private JPanel buildCreateCard() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Create a Profile"));

        c_idField.setEditable(false);
        c_nameField.setEditable(false);
        c_descArea.setLineWrap(true);
        c_descArea.setWrapStyleWord(true);

        JPanel form = buildForm(c_idField, c_nameField, c_emailField, c_skillsField, c_descArea);

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

    // --------------------------------------------------------------- manage --
    private JPanel buildManageCard() {
        JPanel p = new JPanel(new BorderLayout(8, 8));
        p.setBorder(BorderFactory.createTitledBorder("Manage My Profile"));

        m_idField.setEditable(false);
        m_nameField.setEditable(false);
        m_descArea.setLineWrap(true);
        m_descArea.setWrapStyleWord(true);
        setManageEditable(false);

        m_deleteBtn.setForeground(Color.RED);
        m_editBtn.addActionListener(e -> setManageEditable(true));
        m_saveBtn.addActionListener(e -> saveManage());
        m_deleteBtn.addActionListener(e -> deleteProfile());

        JPanel form = buildForm(m_idField, m_nameField, m_emailField, m_skillsField, m_descArea);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton back = new JButton("< Back");
        back.addActionListener(e -> showHub());
        btns.add(back);
        btns.add(m_deleteBtn);
        btns.add(m_editBtn);
        btns.add(m_saveBtn);

        p.add(form, BorderLayout.CENTER);
        p.add(btns, BorderLayout.SOUTH);
        return p;
    }

    // ---------------------------------------------------------- form builder --
    private static JPanel buildForm(JTextField idF, JTextField nameF,
                                    JTextField emailF, JTextField skillsF, JTextArea descA) {
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

        c.gridx = 0; c.gridy = 4; c.anchor = GridBagConstraints.NORTHWEST;
        form.add(new JLabel("Description"), c);
        c.gridx = 1; c.anchor = GridBagConstraints.CENTER;
        form.add(new JScrollPane(descA), c);

        return form;
    }

    // ------------------------------------------------------------- navigate --
    private void showHub() {
        cards.show(deck, CARD_HUB);
    }

    private void showCreate() {
        Applicant a = data.getApplicant(account).orElse(null);
        c_idField.setText(account);
        c_nameField.setText(a != null ? a.name() : "");
        c_emailField.setText("");
        c_skillsField.setText("");
        c_descArea.setText("");
        cards.show(deck, CARD_CREATE);
    }

    private void showManage() {
        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        m_idField.setText(a.id());
        m_nameField.setText(a.name());
        m_emailField.setText(a.email());
        m_skillsField.setText(a.skills());
        m_descArea.setText(a.description());
        setManageEditable(false);
        cards.show(deck, CARD_MANAGE);
    }

    private void setManageEditable(boolean editable) {
        m_emailField.setEditable(editable);
        m_skillsField.setEditable(editable);
        m_descArea.setEditable(editable);
        m_editBtn.setVisible(!editable);
        m_saveBtn.setVisible(editable);
    }

    // --------------------------------------------------------------- actions --
    private void saveCreate() {
        String email  = c_emailField.getText().trim();
        String skills = c_skillsField.getText().trim();
        String desc   = c_descArea.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        data.upsertApplicant(a.withProfile(a.name(), email, skills, a.cvPath(), desc));
        JOptionPane.showMessageDialog(this, "Profile created successfully.");
        showHub();
    }

    private void saveManage() {
        String email  = m_emailField.getText().trim();
        String skills = m_skillsField.getText().trim();
        String desc   = m_descArea.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Email is required", "Validation", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        data.upsertApplicant(a.withProfile(a.name(), email, skills, a.cvPath(), desc));
        JOptionPane.showMessageDialog(this, "Profile updated successfully.");
        setManageEditable(false);
    }

    private void deleteProfile() {
        int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete your profile information?\n(Email, skills and description will be cleared.)",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) return;

        Applicant a = data.getApplicant(account).orElse(null);
        if (a == null) return;
        data.upsertApplicant(a.withProfile(a.name(), "", "", a.cvPath(), ""));
        JOptionPane.showMessageDialog(this, "Profile information deleted.");
        showHub();
    }

    /** Called externally to reload/reset the page. */
    public void load() {
        showHub();
    }
}
