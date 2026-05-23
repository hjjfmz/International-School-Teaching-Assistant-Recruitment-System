package ebu6304.ui.ta;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
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
import ebu6304.ui.UiTheme;

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
        UiTheme.styleTable(table);

        JPanel top = new JPanel(new BorderLayout(0, 10));
        UiTheme.stylePanelCard(top, I18n.t("ta.jobs.title"));

        JLabel hintLabel = new JLabel(I18n.t("ta.jobs.hint"));
        hintLabel.setForeground(UiTheme.MUTED);
        JPanel summary = new JPanel();
        summary.setOpaque(false);
        summary.setLayout(new BoxLayout(summary, BoxLayout.Y_AXIS));
        hintLabel.setAlignmentX(0f);
        statusLabel.setAlignmentX(0f);
        summary.add(hintLabel);
        summary.add(Box.createVerticalStrut(8));
        summary.add(statusLabel);
        top.add(summary, BorderLayout.NORTH);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        actions.setOpaque(false);
        JButton backBtn = new JButton(I18n.t("common.back"));
        UiTheme.styleButton(backBtn);
        UiTheme.styleButton(refreshButton);
        UiTheme.styleButton(detailsButton);
        UiTheme.stylePrimaryButton(applyButton);
        actions.add(backBtn);
        actions.add(refreshButton);
        actions.add(detailsButton);
        actions.add(applyButton);
        top.add(actions, BorderLayout.SOUTH);

        backBtn.addActionListener(e -> { if (onBack != null) onBack.run(); });
        refreshButton.addActionListener(e -> refresh());
        detailsButton.addActionListener(e -> showDetails());
        applyButton.addActionListener(e -> applySelected());

        add(top, BorderLayout.NORTH);
        JScrollPane tableScrollPane = new JScrollPane(table);
        UiTheme.styleScrollPane(tableScrollPane);
        add(tableScrollPane, BorderLayout.CENTER);

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

        String html = buildDetailsHtml(job, recommendation);
        JEditorPane editorPane = new JEditorPane("text/html", html);
        editorPane.setEditable(false);
        editorPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        editorPane.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JScrollPane scrollPane = new JScrollPane(editorPane);
        scrollPane.setPreferredSize(new Dimension(580, 500));

        JDialog dialog = new JDialog(JOptionPane.getFrameForComponent(this), job.title(), true);
        dialog.getContentPane().setLayout(new BorderLayout());
        dialog.getContentPane().add(scrollPane, BorderLayout.CENTER);
        JButton closeButton = new JButton(I18n.t("common.close"));
        closeButton.addActionListener(e -> dialog.dispose());
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        footer.add(closeButton);
        dialog.getContentPane().add(footer, BorderLayout.SOUTH);
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened(java.awt.event.WindowEvent e) {
                SwingUtilities.invokeLater(() -> {
                    editorPane.revalidate();
                    editorPane.repaint();
                });
            }
        });
        dialog.setVisible(true);
    }

    private static String buildDetailsHtml(Job job, JobRecommendationVo rec) {
        int score = rec.matchScore();
        String scoreColor = score >= 85 ? "#2e7d32" : score >= 70 ? "#1565c0" : score >= 50 ? "#e65100" : "#c62828";

        StringBuilder h = new StringBuilder();
        h.append("<html>");

                h.append("<h2><font color='#1565c0'>").append(escapeHtml(job.title())).append("</font></h2>");
        h.append("<hr noshade size='1' color='#dddddd'>");

                h.append("<b>").append(I18n.t("mo.post.label.hours")).append("</b> ").append(job.hoursPerWeek());
        h.append("&nbsp;&nbsp;&nbsp;&nbsp;");
        h.append("<b>").append(I18n.t("mo.post.label.skills")).append("</b> ").append(escapeHtml(job.requiredSkills()));
        if (!job.postedBy().isEmpty()) {
            h.append("&nbsp;&nbsp;&nbsp;&nbsp;");
            h.append("<b>").append(I18n.t("ta.jobs.col.postedby")).append("</b> ").append(escapeHtml(job.postedBy()));
        }
        h.append("<br><br>");

                h.append("<h3><font color='#333333'>").append(I18n.t("ta.jobs.detail.section.match")).append("</font></h3>");
        h.append("<hr noshade size='1' color='#eeeeee'>");

        h.append("<b>").append(I18n.t("ta.jobs.col.match")).append("</b>&nbsp;&nbsp;");
        h.append("<font size='5' color='").append(scoreColor).append("'><b>").append(score).append("</b></font>");
        h.append("<font color='#888888'>/100</font>");
        int barW = (int) (120 * Math.max(0, Math.min(100, score)) / 100.0);
        h.append("&nbsp;&nbsp;<table cellpadding='0' cellspacing='0'><tr>");
        h.append("<td bgcolor='#e0e0e0' width='120' height='14'>");
        h.append("<table cellpadding='0' cellspacing='0'><tr><td bgcolor='").append(scoreColor).append("' width='").append(barW).append("' height='14'></td></tr></table>");
        h.append("</td></tr></table>");
        h.append("<br>");

        String tag = rec.recommendTag();
        if (tag != null && !tag.isEmpty()) {
            h.append("<b>").append(I18n.t("ta.jobs.col.tag")).append("</b>&nbsp;&nbsp;");
            h.append("<font color='#1565c0'><b>").append(escapeHtml(tag)).append("</b></font><br>");
        }

        String reason = rec.recommendReason();
        if (reason != null && !reason.isEmpty()) {
            h.append("<b>").append(I18n.t("ta.jobs.col.reason")).append("</b><br>");
            h.append("<table width='95%' cellpadding='6' cellspacing='0'><tr><td bgcolor='#f5f5f5'>");
            h.append("<font color='#333333'>").append(escapeHtml(reason)).append("</font>");
            h.append("</td></tr></table>");
        }
        h.append("<br>");

                h.append("<h3><font color='#333333'>").append(I18n.t("ta.jobs.detail.section.skills")).append("</font></h3>");
        h.append("<hr noshade size='1' color='#eeeeee'>");

        List<String> matched = rec.matchedSkills();
        List<String> missing = rec.missingSkills();
        if (!matched.isEmpty()) {
            h.append("<font color='#2e7d32'><b>&#10003; ").append(I18n.t("ta.jobs.detail.matched")).append("</b></font><br>");
            for (String s : matched) {
                h.append("&nbsp;&nbsp;&nbsp;&nbsp;<font color='#2e7d32'>&#8226; ").append(escapeHtml(s)).append("</font><br>");
            }
        }
        if (!missing.isEmpty()) {
            if (!matched.isEmpty()) h.append("<br>");
            h.append("<font color='#c62828'><b>&#10007; ").append(I18n.t("ta.jobs.detail.missing")).append("</b></font><br>");
            for (String s : missing) {
                h.append("&nbsp;&nbsp;&nbsp;&nbsp;<font color='#c62828'>&#8226; ").append(escapeHtml(s)).append("</font><br>");
            }
        }
        h.append("<br>");

                String desc = job.description();
        if (desc != null && !desc.isEmpty()) {
            h.append("<h3><font color='#333333'>").append(I18n.t("ta.jobs.detail.section.description")).append("</font></h3>");
            h.append("<hr noshade size='1' color='#eeeeee'>");
            h.append(escapeHtml(desc).replace("\n", "<br>"));
            h.append("<br>");
        }

        // Source note
        h.append("<hr noshade size='1' color='#eeeeee'>");
        h.append("<font size='2' color='#999999'><i>").append(I18n.t("ta.jobs.detail.source")).append("</i></font>");

        h.append("</html>");
        return h.toString();
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
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
