package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.table.JTableHeader;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableCellRenderer;

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;
import ebu6304.util.SkillMatcher;

/**
 * MO Applicants Page - Enhanced with AI Skill Matching, Batch Operations, and Search/Filter.
 * 
 * Features:
 * - AI-powered skill matching percentage display
 * - Batch Accept/Reject operations
 * - Search by name/email
 * - Filter by application status
 * - Sort by match percentage
 */
public final class MoApplicantsPage extends JPanel {

    private final DataService data;
    private final String account;

    private final JComboBox<JobItem> jobsBox = new JComboBox<JobItem>();
    private final JComboBox<String> statusFilter = new JComboBox<String>(new String[] { "All", "SUBMITTED", "ACCEPTED", "REJECTED" });
    private final JTextField searchField = new JTextField(15);
    
    private final DefaultTableModel model;
    private final JTable table;
    private TableRowSorter<DefaultTableModel> sorter;

    public MoApplicantsPage(DataService data, String account) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        // Enhanced table model with skill match column
        model = new DefaultTableModel(new Object[] {
                I18n.t("mo.applicants.col.appid"), I18n.t("mo.applicants.col.taaccount"), I18n.t("mo.applicants.col.taname"), I18n.t("mo.applicants.col.email"), I18n.t("mo.applicants.col.skills"), I18n.t("mo.applicants.col.match"), I18n.t("mo.applicants.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 5) return Integer.class; // Match percentage for sorting
                return String.class;
            }
        };
        
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Enable multi-select
        table.setAutoCreateRowSorter(true);
        styleTable(table);
        table.setDefaultRenderer(Object.class, new ZebraRenderer());
        table.setDefaultRenderer(Integer.class, new MatchPercentRenderer());

        // Custom sorter for proper numeric sorting on match percentage
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);

        // Top panel with filters
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("mo.applicants.title")));
        top.setOpaque(false);

        // Filter panel (left side)
        JPanel filters = new JPanel(new BorderLayout());
        filters.setOpaque(false);
        filters.add(new JLabel(I18n.t("common.job")), BorderLayout.NORTH);
        styleCombo(jobsBox);
        filters.add(jobsBox, BorderLayout.CENTER);
        filters.add(new JLabel(I18n.t("common.status")), BorderLayout.EAST);
        styleCombo(statusFilter);
        filters.add(statusFilter, BorderLayout.SOUTH);
        filters.add(new JLabel(I18n.t("common.search.label")), BorderLayout.WEST);
        styleSearchField(searchField);
        filters.add(searchField, BorderLayout.EAST);
        
        JButton searchBtn = new JButton(I18n.t("common.search"));
        styleActionButton(searchBtn);
        filters.add(searchBtn, BorderLayout.SOUTH);
        
        // Layout: stack filters and actions vertically to avoid any overlap in narrow windows
        JPanel topContent = new JPanel(new BorderLayout());
        topContent.setOpaque(false);
        topContent.add(filters, BorderLayout.NORTH);

        // Action buttons
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 2));
        actions.setOpaque(false);
        JButton refresh = new JButton(I18n.t("common.refresh"));
        JButton openCv = new JButton(I18n.t("mo.applicants.opencv"));
        JButton accept = new JButton(I18n.t("common.accept"));
        JButton reject = new JButton(I18n.t("common.reject"));
        JButton details = new JButton(I18n.t("mo.applicants.viewdetails"));


        styleActionButton(refresh);
        styleActionButton(details);
        styleActionButton(openCv);
        stylePrimaryButton(accept, new Color(0, 191, 165));
        styleDangerButton(reject);
        
        actions.add(refresh);
        actions.add(details);
        actions.add(openCv);
        actions.add(accept);
        actions.add(reject);

        topContent.add(actions, BorderLayout.SOUTH);
        top.add(topContent, BorderLayout.CENTER);


        // Button actions
        refresh.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { refresh(); } });
        jobsBox.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { refresh(); } });
        statusFilter.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { applyFilter(); } });
        searchBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { applyFilter(); } });
        searchField.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { applyFilter(); } });
        accept.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            if (table.getSelectedRowCount() > 1) batchSetStatus(Application.Status.ACCEPTED);
            else setStatusSelected(Application.Status.ACCEPTED);
        } });
        reject.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) {
            if (table.getSelectedRowCount() > 1) batchSetStatus(Application.Status.REJECTED);
            else setStatusSelected(Application.Status.REJECTED);
        } });
        openCv.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { openCv(); } });
        details.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { showDetails(); } });

        add(top, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        if (sp.getViewport() != null) sp.getViewport().setBackground(Color.WHITE);
        add(sp, BorderLayout.CENTER);

        reloadJobs();
        refresh();
    }

    private static void styleTable(JTable t) {
        t.setRowHeight(34);
        t.setShowGrid(false);
        t.setIntercellSpacing(new Dimension(0, 0));
        t.setFillsViewportHeight(true);
        t.setSelectionBackground(new Color(230, 244, 255));
        t.setSelectionForeground(new Color(30, 41, 59));
        t.setBackground(Color.WHITE);

        JTableHeader h = t.getTableHeader();
        h.setReorderingAllowed(false);
        h.setBackground(new Color(248, 250, 252));
        h.setForeground(new Color(71, 85, 105));
        h.setFont(h.getFont().deriveFont(Font.BOLD, 12f));
        h.setPreferredSize(new Dimension(h.getPreferredSize().width, 36));
        h.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(226, 232, 240)));
    }

    private static void styleCombo(JComboBox<?> c) {
        c.setBackground(Color.WHITE);
        c.setPreferredSize(new Dimension(150, 30));
        c.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    }

    private static void styleSearchField(JTextField f) {
        Border b = BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                BorderFactory.createEmptyBorder(2, 8, 2, 8));
        f.setBorder(b);
        f.setPreferredSize(new Dimension(180, 30));
    }

    private static void styleActionButton(JButton b) {
        b.setFocusPainted(false);
        b.setBackground(Color.WHITE);
        b.setForeground(new Color(30, 41, 59));
    }

    private static void stylePrimaryButton(JButton b, Color bg) {
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(Color.WHITE);
    }

    private static void styleDangerButton(JButton b) {
        styleActionButton(b);
        b.setForeground(new Color(220, 38, 38));
    }

    private static final class MatchPercentRenderer extends DefaultTableCellRenderer {
        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected && value instanceof Integer) {
                int pct = ((Integer) value).intValue();
                if (pct >= 70) c.setForeground(new Color(22, 163, 74));
                else if (pct >= 40) c.setForeground(new Color(202, 138, 4));
                else c.setForeground(new Color(220, 38, 38));
                setFont(getFont().deriveFont(Font.BOLD));
            } else if (!isSelected) {
                c.setForeground(new Color(30, 41, 59));
            }
            c.setBackground(isSelected ? table.getSelectionBackground() : (row % 2 == 0 ? Color.WHITE : new Color(249, 251, 253)));
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    private static final class ZebraRenderer extends DefaultTableCellRenderer {
        private static final Color ODD = new Color(255, 255, 255);
        private static final Color EVEN = new Color(249, 251, 253);

        @Override
        public java.awt.Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column) {
            java.awt.Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (!isSelected) {
                c.setBackground((row % 2 == 0) ? ODD : EVEN);
                c.setForeground(new Color(30, 41, 59));
            }
            setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            return c;
        }
    }

    public void reloadJobs() {
        jobsBox.removeAllItems();
        for (Job j : data.listJobs()) {
            if (!account.equals(j.postedBy())) continue;
            jobsBox.addItem(new JobItem(j.id(), j.title()));
        }
    }

    public void refresh() {
        model.setRowCount(0);
        JobItem it = (JobItem) jobsBox.getSelectedItem();
        if (it == null) return;

        Job selectedJob = data.getJob(it.id).orElse(null);
        String requiredSkills = selectedJob != null ? selectedJob.requiredSkills() : "";

        List<Application> apps = data.listApplicationsForJob(it.id);
        for (Application a : apps) {
            Applicant ta = data.getApplicant(a.applicantId()).orElse(null);
            if (ta == null) continue;
            
            // Calculate skill match percentage
            int matchPercent = SkillMatcher.calculateMatchPercentage(requiredSkills, ta.skills());
            
            model.addRow(new Object[] {
                    a.id(), 
                    a.applicantId(), 
                    ta.name(), 
                    ta.email(), 
                    ta.skills(), 
                    Integer.valueOf(matchPercent),
                    a.status().name()
            });
        }
        
        // Sort by match percentage (descending) by default
        sorter.setSortKeys(java.util.Collections.singletonList(new RowSorter.SortKey(5, javax.swing.SortOrder.DESCENDING)));
        applyFilter();
    }

    /**
     * Apply search and status filters to the table.
     */
    private void applyFilter() {
        String searchText = searchField.getText().trim().toLowerCase();
        String statusValue = (String) statusFilter.getSelectedItem();
        
        // Build filter based on search text and status
        java.util.List<RowFilter<DefaultTableModel, Integer>> filters = new java.util.ArrayList<RowFilter<DefaultTableModel, Integer>>();
        
        // Status filter
        if (statusValue != null && !"All".equals(statusValue)) {
            final String status = statusValue;
            filters.add(RowFilter.regexFilter("^" + status + "$", 6));
        }
        
        // Search filter (search in name, email, and account columns)
        if (!searchText.isEmpty()) {
            java.util.List<RowFilter<DefaultTableModel, Integer>> searchFilters = new java.util.ArrayList<RowFilter<DefaultTableModel, Integer>>();
            searchFilters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 1)); // Account
            searchFilters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 2)); // Name
            searchFilters.add(RowFilter.regexFilter("(?i)" + java.util.regex.Pattern.quote(searchText), 3)); // Email
            filters.add(RowFilter.orFilter(searchFilters));
        }
        
        if (filters.isEmpty()) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.andFilter(filters));
        }
    }

    /**
     * Set status for a single selected applicant.
     */
    private void setStatusSelected(Application.Status st) {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }
        
        // Convert view row index to model row index
        int modelRow = table.convertRowIndexToModel(r);
        String appId = String.valueOf(model.getValueAt(modelRow, 0));
        
        data.setApplicationStatus(account, appId, st);
        JOptionPane.showMessageDialog(this, I18n.t("msg.status.updated", st.name()));
        refresh();
    }

    /**
     * Batch set status for multiple selected applicants.
     */
    private void batchSetStatus(Application.Status st) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, I18n.t("mo.applicants.batch.multiselect"));
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            I18n.t("mo.applicants.batch.confirm", st.name(), selectedRows.length), 
            I18n.t("mo.applicants.batch.title"), 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        int success = 0;
        for (int r : selectedRows) {
            int modelRow = table.convertRowIndexToModel(r);
            String appId = String.valueOf(model.getValueAt(modelRow, 0));
            data.setApplicationStatus(account, appId, st);
            success++;
        }
        
        JOptionPane.showMessageDialog(this, I18n.t("msg.status.updated.batch", success, st.name()));
        refresh();
    }

    /**
     * Open CV file for selected applicant.
     */
    private void openCv() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }
        
        // Get the applicant ID
        int modelRow = table.convertRowIndexToModel(r);
        String taAccount = String.valueOf(model.getValueAt(modelRow, 1));
        
        Applicant ta = data.getApplicant(taAccount).orElse(null);
        if (ta == null || ta.cvPath() == null || ta.cvPath().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.t("mo.applicants.nocv"));
            return;
        }
        
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(ta.cvPath()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("mo.applicants.cv.openfailed") + ex.getMessage());
        }
    }

    /**
     * Show detailed applicant information with skill match analysis.
     */
    private void showDetails() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(r);
        String taAccount = String.valueOf(model.getValueAt(modelRow, 1));
        String appStatus = String.valueOf(model.getValueAt(modelRow, 6));
        
        Applicant ta = data.getApplicant(taAccount).orElse(null);
        JobItem it = (JobItem) jobsBox.getSelectedItem();
        Job job = it != null ? data.getJob(it.id).orElse(null) : null;
        
        if (ta == null || job == null) return;
        
        // Build detailed info
        StringBuilder sb = new StringBuilder();
        sb.append("=== Applicant Details ===\n\n");
        sb.append("Name: ").append(ta.name()).append("\n");
        sb.append("Account: ").append(ta.id()).append("\n");
        sb.append("Email: ").append(ta.email()).append("\n");
        sb.append("Skills: ").append(ta.skills()).append("\n");
        sb.append("CV Path: ").append(ta.cvPath() != null ? ta.cvPath() : "Not uploaded").append("\n\n");
        
        sb.append("=== Job Requirements ===\n\n");
        sb.append("Position: ").append(job.title()).append("\n");
        sb.append("Required Skills: ").append(job.requiredSkills()).append("\n");
        sb.append("Hours/Week: ").append(job.hoursPerWeek()).append("\n\n");
        
        sb.append("=== AI Skill Match Analysis ===\n\n");
        int matchPercent = SkillMatcher.calculateMatchPercentage(job.requiredSkills(), ta.skills());
        sb.append("Match Score: ").append(matchPercent).append("%\n");
        
        List<String> matching = SkillMatcher.getMatchingSkills(job.requiredSkills(), ta.skills());
        List<String> missing = SkillMatcher.getMissingSkills(job.requiredSkills(), ta.skills());
        
        if (!matching.isEmpty()) {
            sb.append("Matching Skills: ").append(String.join(", ", matching)).append("\n");
        }
        if (!missing.isEmpty()) {
            sb.append("Missing Skills: ").append(String.join(", ", missing)).append("\n");
        }
        
        sb.append("\n=== Application Status ===\n\n");
        sb.append("Status: ").append(appStatus).append("\n");
        
        // Create a larger dialog for details
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setRows(20);
        textArea.setColumns(50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, I18n.t("mo.applicants.details.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    /**
     * Job item for combo box.
     */
    private static final class JobItem {
        private final String id;
        private final String title;

        private JobItem(String id, String title) {
            this.id = id;
            this.title = title;
        }

        @Override
        public String toString() {
            return title;
        }
    }
}
