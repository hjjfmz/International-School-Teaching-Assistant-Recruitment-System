package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
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
                "Application ID", "TA Account", "TA Name", "Email", "Skills", "Match %", "Status"
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
        
        // Custom sorter for proper numeric sorting on match percentage
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);

        // Top panel with filters
        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder("Applicants"));

        // Filter panel (left side)
        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel("Job:"));
        filters.add(jobsBox);
        filters.add(new JLabel("Status:"));
        filters.add(statusFilter);
        filters.add(new JLabel("Search:"));
        filters.add(searchField);
        
        JButton searchBtn = new JButton("Search");
        filters.add(searchBtn);
        
        top.add(filters, BorderLayout.WEST);

        // Action buttons (right side)
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refresh = new JButton("Refresh");
        JButton openCv = new JButton("Open CV");
        JButton accept = new JButton("Accept");
        JButton reject = new JButton("Reject");
        JButton batchAccept = new JButton("Batch Accept");
        JButton batchReject = new JButton("Batch Reject");
        JButton details = new JButton("View Details");
        
        actions.add(refresh);
        actions.add(details);
        actions.add(openCv);
        actions.add(accept);
        actions.add(reject);
        actions.add(batchAccept);
        actions.add(batchReject);
        top.add(actions, BorderLayout.EAST);

        // Button actions
        refresh.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { refresh(); } });
        jobsBox.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { refresh(); } });
        statusFilter.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { applyFilter(); } });
        searchBtn.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { applyFilter(); } });
        accept.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { setStatusSelected(Application.Status.ACCEPTED); } });
        reject.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { setStatusSelected(Application.Status.REJECTED); } });
        batchAccept.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { batchSetStatus(Application.Status.ACCEPTED); } });
        batchReject.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { batchSetStatus(Application.Status.REJECTED); } });
        openCv.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { openCv(); } });
        details.addActionListener(new ActionListener() { public void actionPerformed(ActionEvent e) { showDetails(); } });

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reloadJobs();
        refresh();
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
            JOptionPane.showMessageDialog(this, "Please select an applicant");
            return;
        }
        
        // Convert view row index to model row index
        int modelRow = table.convertRowIndexToModel(r);
        String appId = String.valueOf(model.getValueAt(modelRow, 0));
        
        data.setApplicationStatus(appId, st);
        JOptionPane.showMessageDialog(this, "Application " + st.name());
        refresh();
    }

    /**
     * Batch set status for multiple selected applicants.
     */
    private void batchSetStatus(Application.Status st) {
        int[] selectedRows = table.getSelectedRows();
        if (selectedRows.length == 0) {
            JOptionPane.showMessageDialog(this, "Please select one or more applicants (hold Ctrl or Shift to multi-select)");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this, 
            "Are you sure you want to " + st.name() + " " + selectedRows.length + " applicant(s)?", 
            "Confirm Batch Operation", 
            JOptionPane.YES_NO_OPTION);
        
        if (confirm != JOptionPane.YES_OPTION) return;
        
        int success = 0;
        for (int r : selectedRows) {
            int modelRow = table.convertRowIndexToModel(r);
            String appId = String.valueOf(model.getValueAt(modelRow, 0));
            data.setApplicationStatus(appId, st);
            success++;
        }
        
        JOptionPane.showMessageDialog(this, success + " application(s) " + st.name());
        refresh();
    }

    /**
     * Open CV file for selected applicant.
     */
    private void openCv() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant");
            return;
        }
        
        // Get the applicant ID
        int modelRow = table.convertRowIndexToModel(r);
        String taAccount = String.valueOf(model.getValueAt(modelRow, 1));
        
        Applicant ta = data.getApplicant(taAccount).orElse(null);
        if (ta == null || ta.cvPath() == null || ta.cvPath().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No CV available for this applicant");
            return;
        }
        
        try {
            java.awt.Desktop.getDesktop().open(new java.io.File(ta.cvPath()));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to open CV file: " + ex.getMessage());
        }
    }

    /**
     * Show detailed applicant information with skill match analysis.
     */
    private void showDetails() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Please select an applicant");
            return;
        }
        
        int modelRow = table.convertRowIndexToModel(r);
        String appId = String.valueOf(model.getValueAt(modelRow, 0));
        String taAccount = String.valueOf(model.getValueAt(modelRow, 1));
        
        Application app = null;
        for (Application a : data.listApplicationsForApplicant(taAccount)) {
            if (a.id().equals(appId)) {
                app = a;
                break;
            }
        }
        
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
        sb.append("Status: ").append(app != null ? app.status().name() : "Unknown").append("\n");
        
        // Create a larger dialog for details
        javax.swing.JTextArea textArea = new javax.swing.JTextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setRows(20);
        textArea.setColumns(50);
        JScrollPane scrollPane = new JScrollPane(textArea);
        JOptionPane.showMessageDialog(this, scrollPane, "Applicant Details", JOptionPane.INFORMATION_MESSAGE);
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