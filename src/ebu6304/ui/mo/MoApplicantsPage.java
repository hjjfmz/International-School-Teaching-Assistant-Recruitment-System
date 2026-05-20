package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.controller.ApplicantMatchController;
import ebu6304.ai.vo.JobMatchResultVo;
import ebu6304.model.Applicant;
import ebu6304.model.Application;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class MoApplicantsPage extends JPanel {
    private final DataService data;
    private final String account;
    private final ApplicantMatchController applicantMatchController;

    private final JComboBox<JobItem> jobsBox = new JComboBox<JobItem>();
    private final JComboBox<String> statusFilter = new JComboBox<String>(
            new String[] { "ALL", Application.Status.SUBMITTED.name(), Application.Status.ACCEPTED.name(), Application.Status.REJECTED.name() });
    private final JTextField searchField = new JTextField(16);

    private final DefaultTableModel model;
    private final JTable table;
    private final TableRowSorter<DefaultTableModel> sorter;

    private final JButton refreshButton = new JButton(I18n.t("common.refresh"));
    private final JButton detailsButton = new JButton(I18n.t("mo.applicants.viewdetails"));
    private final JButton openCvButton = new JButton(I18n.t("mo.applicants.opencv"));
    private final JButton explainButton = new JButton(I18n.t("mo.applicants.ai.explain"));
    private final JButton acceptButton = new JButton(I18n.t("common.accept"));
    private final JButton rejectButton = new JButton(I18n.t("common.reject"));
    private final JLabel statusLabel = new JLabel(I18n.t("mo.applicants.loading.idle"));

    private final List<RowData> rows = new ArrayList<RowData>();

    public MoApplicantsPage(DataService data, String account, ApplicantMatchController applicantMatchController) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        this.applicantMatchController = applicantMatchController;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                I18n.t("mo.applicants.col.appid"),
                I18n.t("mo.applicants.col.taaccount"),
                I18n.t("mo.applicants.col.taname"),
                I18n.t("mo.applicants.col.email"),
                I18n.t("mo.applicants.col.skills"),
                I18n.t("mo.applicants.col.match"),
                I18n.t("mo.applicants.col.tag"),
                I18n.t("mo.applicants.col.status")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        sorter = new TableRowSorter<DefaultTableModel>(model);
        table.setRowSorter(sorter);
        sorter.setComparator(5, (a, b) -> Integer.compare(asInt(b), asInt(a)));

        JPanel top = new JPanel(new BorderLayout(8, 8));
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("mo.applicants.title")));

        JPanel filters = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filters.add(new JLabel(I18n.t("common.job")));
        filters.add(jobsBox);
        filters.add(new JLabel(I18n.t("common.status")));
        filters.add(statusFilter);
        filters.add(new JLabel(I18n.t("common.search.label")));
        filters.add(searchField);
        JButton searchButton = new JButton(I18n.t("common.search"));
        filters.add(searchButton);
        top.add(filters, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(refreshButton);
        actions.add(detailsButton);
        actions.add(openCvButton);
        actions.add(explainButton);
        actions.add(acceptButton);
        actions.add(rejectButton);
        top.add(actions, BorderLayout.SOUTH);
        top.add(statusLabel, BorderLayout.CENTER);

        refreshButton.addActionListener(e -> refresh());
        jobsBox.addActionListener(e -> refresh());
        statusFilter.addActionListener(e -> applyFilter());
        searchButton.addActionListener(e -> applyFilter());
        searchField.addActionListener(e -> applyFilter());
        detailsButton.addActionListener(e -> showDetails());
        openCvButton.addActionListener(e -> openCv());
        explainButton.addActionListener(e -> explainSelection());
        acceptButton.addActionListener(e -> updateSelectedStatus(Application.Status.ACCEPTED));
        rejectButton.addActionListener(e -> updateSelectedStatus(Application.Status.REJECTED));

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        reloadJobs();
        refresh();
    }

    public void reloadJobs() {
        jobsBox.removeAllItems();
        for (Job job : data.listJobs()) {
            jobsBox.addItem(new JobItem(job.id(), job.title()));
        }
    }

    public void refresh() {
        final Job job = selectedJob();
        rows.clear();
        model.setRowCount(0);
        if (job == null) {
            statusLabel.setText(I18n.t("mo.applicants.loading.empty"));
            return;
        }

        setBusy(true);
        statusLabel.setText(I18n.t("mo.applicants.loading"));
        SwingWorker<List<RowData>, Void> worker = new SwingWorker<List<RowData>, Void>() {
            @Override
            protected List<RowData> doInBackground() {
                List<RowData> loaded = new ArrayList<RowData>();
                for (Application application : data.listApplicationsForJob(job.id())) {
                    Applicant applicant = data.getApplicant(application.applicantId()).orElse(null);
                    if (applicant == null) continue;
                    JobMatchResultVo match = applicantMatchController == null
                            ? new JobMatchResultVo(applicant.id(), job.id(), application.aiScore() < 0 ? 0 : application.aiScore(),
                                    0, 0, 0, null, null, null, "", "")
                            : applicantMatchController.evaluateFast(job, applicant);
                    loaded.add(new RowData(application, applicant, job, match));
                }
                return loaded;
            }

            @Override
            protected void done() {
                try {
                    rows.clear();
                    rows.addAll(get());
                    model.setRowCount(0);
                    for (RowData row : rows) {
                        model.addRow(toRow(row));
                    }
                    applyFilter();
                    statusLabel.setText(I18n.t("mo.applicants.loading.done", Integer.valueOf(rows.size())));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText(I18n.t("mo.applicants.loading.failed"));
                } catch (ExecutionException ex) {
                    statusLabel.setText(I18n.t("mo.applicants.loading.failed"));
                    JOptionPane.showMessageDialog(MoApplicantsPage.this,
                            I18n.t("msg.operation.failed") + ": " + rootCause(ex).getMessage());
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private Object[] toRow(RowData row) {
        return new Object[] {
                row.application.id(),
                row.applicant.id(),
                row.applicant.name(),
                row.applicant.email(),
                row.applicant.skills(),
                Integer.valueOf(row.match.overallScore()),
                row.match.recommendTag(),
                row.application.status().name()
        };
    }

    private void applyFilter() {
        final String status = String.valueOf(statusFilter.getSelectedItem());
        final String keyword = searchField.getText() == null ? "" : searchField.getText().trim().toLowerCase();
        sorter.setRowFilter(new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                int modelRow = entry.getIdentifier().intValue();
                if (modelRow < 0 || modelRow >= rows.size()) return false;
                RowData row = rows.get(modelRow);
                if (!"ALL".equalsIgnoreCase(status) && !row.application.status().name().equalsIgnoreCase(status)) return false;
                if (keyword.isEmpty()) return true;
                return row.applicant.name().toLowerCase().contains(keyword)
                        || row.applicant.email().toLowerCase().contains(keyword)
                        || row.applicant.skills().toLowerCase().contains(keyword);
            }
        });
    }

    private void showDetails() {
        RowData row = selectedSingleRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }
        JobMatchResultVo match = row.match;
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("mo.applicants.col.taname")).append(": ").append(row.applicant.name()).append("\n");
        sb.append(I18n.t("mo.applicants.col.email")).append(": ").append(row.applicant.email()).append("\n");
        sb.append(I18n.t("mo.applicants.col.skills")).append(": ").append(row.applicant.skills()).append("\n");
        sb.append(I18n.t("mo.applicants.detail.overall")).append(": ").append(match.overallScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.skill")).append(": ").append(match.skillScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.seniority")).append(": ").append(match.seniorityScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.domain")).append(": ").append(match.domainScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.matched")).append(": ").append(formatList(match.matchedSkills())).append("\n");
        sb.append(I18n.t("mo.applicants.detail.missing")).append(": ").append(formatList(match.missingSkills())).append("\n");
        sb.append(I18n.t("mo.applicants.col.tag")).append(": ").append(nonBlank(match.recommendTag())).append("\n");
        sb.append(I18n.t("mo.applicants.detail.reason")).append(": ").append(nonBlank(match.shortReason())).append("\n");
        sb.append("\n").append(I18n.t("mo.applicants.detail.source"));
        JOptionPane.showMessageDialog(this, sb.toString(), I18n.t("mo.applicants.details.title"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void explainSelection() {
        final RowData row = selectedSingleRow();
        final int rowIndex = selectedModelRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }

        setBusy(true);
        statusLabel.setText(I18n.t("mo.applicants.ai.streaming"));

        final JTextArea streamArea = new JTextArea();
        streamArea.setEditable(false);
        streamArea.setLineWrap(true);
        streamArea.setWrapStyleWord(true);
        streamArea.setText(I18n.t("mo.applicants.ai.streaming.start"));
        final JDialog streamDialog = buildStreamingDialog(streamArea);
        streamDialog.setVisible(true);

        SwingWorker<JobMatchResultVo, String> worker = new SwingWorker<JobMatchResultVo, String>() {
            @Override
            protected JobMatchResultVo doInBackground() {
                return applicantMatchController.evaluateWithStreaming(row.job, row.applicant, row.application.id(), new AiStreamListener() {
                    @Override
                    public void onDelta(String delta) {
                        publish(delta);
                    }
                });
            }

            @Override
            protected void process(List<String> chunks) {
                for (String chunk : chunks) {
                    if (chunk == null || chunk.isEmpty()) continue;
                    streamArea.append(chunk);
                }
                streamArea.setCaretPosition(streamArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    row.match = get();
                    if (rowIndex >= 0 && rowIndex < model.getRowCount()) {
                        model.setValueAt(Integer.valueOf(row.match.overallScore()), rowIndex, 5);
                        model.setValueAt(row.match.recommendTag(), rowIndex, 6);
                    }
                    streamArea.setText(formatExplain(row.match));
                    streamArea.setCaretPosition(0);
                    statusLabel.setText(I18n.t("mo.applicants.ai.streaming.done"));
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    streamDialog.dispose();
                } catch (ExecutionException ex) {
                    streamDialog.dispose();
                    statusLabel.setText(I18n.t("mo.applicants.loading.failed"));
                    JOptionPane.showMessageDialog(MoApplicantsPage.this,
                            I18n.t("msg.operation.failed") + ": " + rootCause(ex).getMessage());
                } finally {
                    setBusy(false);
                }
            }
        };
        worker.execute();
    }

    private JDialog buildStreamingDialog(JTextArea streamArea) {
        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(this), I18n.t("mo.applicants.ai.explain"), false);
        JScrollPane scrollPane = new JScrollPane(streamArea);
        scrollPane.setPreferredSize(new Dimension(560, 360));
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        JButton closeButton = new JButton(I18n.t("common.close"));
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(closeButton);
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private void openCv() {
        RowData row = selectedSingleRow();
        if (row == null) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }
        String path = row.applicant.cvPath();
        if (path == null || path.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, I18n.t("mo.applicants.nocv"));
            return;
        }
        try {
            java.awt.Desktop.getDesktop().open(new File(path));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, I18n.t("mo.applicants.cv.openfailed") + path);
        }
    }

    private void updateSelectedStatus(Application.Status status) {
        int[] selected = table.getSelectedRows();
        if (selected == null || selected.length == 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.applicant"));
            return;
        }
        int confirm = JOptionPane.showConfirmDialog(this,
                I18n.t("mo.applicants.batch.confirm", status.name(), Integer.valueOf(selected.length)),
                I18n.t("mo.applicants.batch.title"),
                JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        for (int rowIndex : selected) {
            int modelRow = table.convertRowIndexToModel(rowIndex);
            if (modelRow < 0 || modelRow >= rows.size()) continue;
            RowData row = rows.get(modelRow);
            data.setApplicationStatus(account, row.application.id(), status);
        }
        refresh();
    }

    private Job selectedJob() {
        JobItem item = (JobItem) jobsBox.getSelectedItem();
        if (item == null) return null;
        return data.getJob(item.id).orElse(null);
    }

    private RowData selectedSingleRow() {
        int modelRow = selectedModelRow();
        if (modelRow < 0 || modelRow >= rows.size()) return null;
        return rows.get(modelRow);
    }

    private int selectedModelRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }

    private void setBusy(boolean busy) {
        refreshButton.setEnabled(!busy);
        detailsButton.setEnabled(!busy);
        openCvButton.setEnabled(!busy);
        explainButton.setEnabled(!busy);
        acceptButton.setEnabled(!busy);
        rejectButton.setEnabled(!busy);
    }

    private static int asInt(Object value) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value == null) return 0;
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (RuntimeException ex) {
            return 0;
        }
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable current = ex;
        while (current != null && current.getCause() != null) {
            current = current.getCause();
        }
        return current == null ? ex : current;
    }

    private static String formatList(List<String> items) {
        if (items == null || items.isEmpty()) return "-";
        return String.join(", ", items);
    }

    private static String formatMultilineList(List<String> items) {
        if (items == null || items.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (item == null || item.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append("- ").append(item.trim());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private static String formatExplain(JobMatchResultVo match) {
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("mo.applicants.detail.overall")).append(": ").append(match == null ? 0 : match.overallScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.skill")).append(": ").append(match == null ? 0 : match.skillScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.seniority")).append(": ").append(match == null ? 0 : match.seniorityScore()).append("/100\n");
        sb.append(I18n.t("mo.applicants.detail.domain")).append(": ").append(match == null ? 0 : match.domainScore()).append("/100\n\n");
        sb.append(I18n.t("mo.applicants.col.tag")).append(": ").append(match == null ? "-" : nonBlank(match.recommendTag())).append("\n");
        sb.append(I18n.t("mo.applicants.detail.reason")).append(": ").append(match == null ? "-" : nonBlank(match.shortReason())).append("\n");
        sb.append(I18n.t("mo.applicants.detail.source")).append("\n\n");
        sb.append(formatMultilineList(match == null ? null : match.recommendReasons()));
        return sb.toString();
    }

    private static String nonBlank(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }

    private static final class JobItem {
        private final String id;
        private final String label;

        private JobItem(String id, String label) {
            this.id = id;
            this.label = label == null ? "" : label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static final class RowData {
        private final Application application;
        private final Applicant applicant;
        private final Job job;
        private JobMatchResultVo match;

        private RowData(Application application, Applicant applicant, Job job, JobMatchResultVo match) {
            this.application = application;
            this.applicant = applicant;
            this.job = job;
            this.match = match;
        }
    }
}
