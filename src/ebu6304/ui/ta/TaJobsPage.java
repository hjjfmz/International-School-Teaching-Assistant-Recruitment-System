package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.controller.JobRecommendationController;
import ebu6304.ai.vo.JobRecommendationVo;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class TaJobsPage extends JPanel {
    private final DataService data;
    private final String account;
    private final JobRecommendationController recommendationController;

    private final DefaultTableModel model;
    private final JTable table;
    private final JLabel statusLabel = new JLabel(I18n.t("ta.jobs.loading.idle"));

    private final JButton refreshButton = new JButton(I18n.t("common.refresh"));
    private final JButton detailsButton = new JButton(I18n.t("common.details"));
    private final JButton applyButton = new JButton(I18n.t("common.apply"));

    private List<JobRecommendationVo> currentRecommendations = new ArrayList<JobRecommendationVo>();
    private int refreshVersion = 0;

    public TaJobsPage(DataService data, String account, JobRecommendationController recommendationController, Runnable onBack) {
        super(new BorderLayout(10, 10));
        this.data = data;
        this.account = account;
        this.recommendationController = recommendationController;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        model = new DefaultTableModel(new Object[] {
                I18n.t("ta.jobs.col.id"),
                I18n.t("ta.jobs.col.title"),
                I18n.t("ta.jobs.col.skills"),
                I18n.t("ta.jobs.col.hours"),
                I18n.t("ta.jobs.col.match"),
                I18n.t("ta.jobs.col.tag"),
                I18n.t("ta.jobs.col.reason")
        }, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);

        JPanel top = new JPanel(new BorderLayout());
        top.setBorder(BorderFactory.createTitledBorder(I18n.t("ta.jobs.title")));
        top.add(new JLabel(I18n.t("ta.jobs.hint")), BorderLayout.WEST);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton(I18n.t("common.back"));
        actions.add(backBtn);
        actions.add(refreshButton);
        actions.add(detailsButton);
        actions.add(applyButton);
        top.add(actions, BorderLayout.EAST);
        top.add(statusLabel, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });
        refreshButton.addActionListener(e -> refresh());
        detailsButton.addActionListener(e -> showDetails());
        applyButton.addActionListener(e -> applySelected());

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        refresh();
    }

    public void refresh() {
        final int version = ++refreshVersion;
        setBusy(true, I18n.t("ta.jobs.loading"));
        SwingWorker<List<JobRecommendationVo>, Void> worker = new SwingWorker<List<JobRecommendationVo>, Void>() {
            @Override
            protected List<JobRecommendationVo> doInBackground() {
                List<JobRecommendationVo> recommendations = recommendationController == null
                        ? new ArrayList<JobRecommendationVo>()
                        : recommendationController.recommendJobsFast(account);
                if (!recommendations.isEmpty()) return recommendations;

                List<JobRecommendationVo> fallback = new ArrayList<JobRecommendationVo>();
                for (Job job : data.listJobs()) {
                    fallback.add(new JobRecommendationVo(job, 0, I18n.t("ta.jobs.tag.profile"),
                            I18n.t("ta.jobs.reason.profile"), null, null));
                }
                return fallback;
            }

            @Override
            protected void done() {
                try {
                    if (version != refreshVersion) return;
                    currentRecommendations = get();
                    repaintRows();
                    statusLabel.setText(I18n.t("ta.jobs.loading.done", Integer.valueOf(currentRecommendations.size())));
                    setBusy(false, statusLabel.getText());
                    streamTopReasons(version);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    statusLabel.setText(I18n.t("ta.jobs.loading.failed"));
                    setBusy(false, statusLabel.getText());
                } catch (ExecutionException ex) {
                    statusLabel.setText(I18n.t("ta.jobs.loading.failed"));
                    JOptionPane.showMessageDialog(TaJobsPage.this,
                            I18n.t("msg.operation.failed") + ": " + rootCause(ex).getMessage());
                    setBusy(false, statusLabel.getText());
                }
            }
        };
        worker.execute();
    }

    private void repaintRows() {
        model.setRowCount(0);
        for (JobRecommendationVo recommendation : currentRecommendations) {
            Job job = recommendation.job();
            if (job == null) continue;
            model.addRow(new Object[] {
                    job.id(),
                    job.title(),
                    job.requiredSkills(),
                    Integer.valueOf(job.hoursPerWeek()),
                    Integer.valueOf(recommendation.matchScore()),
                    recommendation.recommendTag(),
                    recommendation.recommendReason()
            });
        }
    }

    private void streamTopReasons(final int version) {
        if (recommendationController == null || currentRecommendations.isEmpty()) return;
        final int limit = Math.min(3, currentRecommendations.size());
        if (limit <= 0) return;

        statusLabel.setText(I18n.t("ta.jobs.loading.ai", Integer.valueOf(limit)));
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() {
                for (int i = 0; i < limit; i++) {
                    if (version != refreshVersion) break;
                    JobRecommendationVo base = currentRecommendations.get(i);
                    Job job = base == null ? null : base.job();
                    if (job == null) continue;
                    final int rowIndex = i;
                    final int displayIndex = i + 1;
                    final String title = job.title();
                    JobRecommendationVo enriched = recommendationController.enrichRecommendation(account, job.id(), new AiStreamListener() {
                        private int chunkCount = 0;

                        @Override
                        public void onStart() {
                            updateStreamingStatus(version, I18n.t("ta.jobs.loading.ai.item",
                                    Integer.valueOf(displayIndex), Integer.valueOf(limit), title));
                        }

                        @Override
                        public void onDelta(String delta) {
                            chunkCount++;
                            if (chunkCount == 1 || chunkCount % 10 == 0) {
                                updateStreamingStatus(version, I18n.t("ta.jobs.loading.ai.item",
                                        Integer.valueOf(displayIndex), Integer.valueOf(limit), title));
                            }
                        }
                    });
                    if (enriched != null) {
                        final JobRecommendationVo finalEnriched = enriched;
                        SwingUtilities.invokeLater(() -> {
                            if (version != refreshVersion || rowIndex >= currentRecommendations.size()) return;
                            currentRecommendations.set(rowIndex, finalEnriched);
                            model.setValueAt(finalEnriched.recommendTag(), rowIndex, 5);
                            model.setValueAt(finalEnriched.recommendReason(), rowIndex, 6);
                        });
                    }
                }
                SwingUtilities.invokeLater(() -> {
                    if (version == refreshVersion) {
                        statusLabel.setText(I18n.t("ta.jobs.loading.ai.done", Integer.valueOf(limit)));
                    }
                });
                return null;
            }
        };
        worker.execute();
    }

    private void updateStreamingStatus(final int version, final String text) {
        SwingUtilities.invokeLater(() -> {
            if (version == refreshVersion) statusLabel.setText(text);
        });
    }

    private void showDetails() {
        int modelRow = selectedModelRow();
        if (modelRow < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.job"));
            return;
        }
        JobRecommendationVo recommendation = currentRecommendations.get(modelRow);
        Job job = recommendation.job();
        if (job == null) return;

        StringBuilder msg = new StringBuilder();
        msg.append(I18n.t("mo.post.label.title")).append(" ").append(job.title()).append("\n");
        msg.append(I18n.t("mo.post.label.hours")).append(" ").append(job.hoursPerWeek()).append("\n");
        msg.append(I18n.t("mo.post.label.skills")).append(" ").append(job.requiredSkills()).append("\n");
        msg.append(I18n.t("ta.jobs.col.match")).append(" ").append(recommendation.matchScore()).append("/100\n");
        msg.append(I18n.t("ta.jobs.col.tag")).append(" ").append(nonBlank(recommendation.recommendTag())).append("\n");
        msg.append(I18n.t("ta.jobs.col.reason")).append(" ").append(nonBlank(recommendation.recommendReason())).append("\n\n");
        msg.append(job.description()).append("\n\n");
        msg.append(I18n.t("ta.jobs.detail.matched")).append(" ").append(formatList(recommendation.matchedSkills())).append("\n");
        msg.append(I18n.t("ta.jobs.detail.missing")).append(" ").append(formatList(recommendation.missingSkills()));
        JOptionPane.showMessageDialog(this, msg.toString(), I18n.t("common.details"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void applySelected() {
        int modelRow = selectedModelRow();
        if (modelRow < 0) {
            JOptionPane.showMessageDialog(this, I18n.t("msg.select.job"));
            return;
        }
        Job job = currentRecommendations.get(modelRow).job();
        if (job == null) return;
        String jobId = job.id();

        if (data.findApplication(account, jobId).isPresent()) {
            JOptionPane.showMessageDialog(this, I18n.t("ta.jobs.already.applied"));
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this, I18n.t("ta.jobs.confirm.apply"),
                I18n.t("common.confirm"), JOptionPane.YES_NO_OPTION);
        if (ok != JOptionPane.YES_OPTION) return;

        data.submitApplication(account, jobId);
        JOptionPane.showMessageDialog(this, I18n.t("ta.jobs.applied"));
    }

    private int selectedModelRow() {
        int row = table.getSelectedRow();
        return row < 0 ? -1 : table.convertRowIndexToModel(row);
    }

    private void setBusy(boolean busy, String status) {
        refreshButton.setEnabled(!busy);
        detailsButton.setEnabled(!busy);
        applyButton.setEnabled(!busy);
        statusLabel.setText(status == null ? "" : status);
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

    private static String nonBlank(String value) {
        return value == null || value.trim().isEmpty() ? "-" : value.trim();
    }
}
