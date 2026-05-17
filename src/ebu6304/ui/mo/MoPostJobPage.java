package ebu6304.ui.mo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.List;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;

import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.controller.AiIndexController;
import ebu6304.ai.controller.JdAssistantController;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.vo.JdPolishResultVo;
import ebu6304.ai.vo.JdQualityIssueVo;
import ebu6304.ai.vo.JdQualityResultVo;
import ebu6304.model.Job;
import ebu6304.storage.DataService;
import ebu6304.ui.I18n;

public final class MoPostJobPage extends JPanel {
    private final DataService data;
    private final String account;
    private final JdAssistantController jdAssistantController;
    private final AiIndexController aiIndexController;

    private final JTextField titleField = new JTextField(22);
    private final JTextField skillsField = new JTextField(22);
    private final JTextField hoursField = new JTextField(6);
    private final JTextArea descArea = new JTextArea(8, 22);
    private final JTextArea aiOutputArea = new JTextArea(12, 60);
    private final JLabel aiStatusLabel = new JLabel(I18n.t("mo.post.ai.idle"));

    private final JButton previewButton = new JButton(I18n.t("common.preview"));
    private final JButton aiCheckButton = new JButton(I18n.t("mo.post.ai.check"));
    private final JButton aiPolishButton = new JButton(I18n.t("mo.post.ai.polish"));
    private final JButton submitButton = new JButton(I18n.t("common.submit"));

    public MoPostJobPage(DataService data, String account, JdAssistantController jdAssistantController, AiIndexController aiIndexController) {
        super(new BorderLayout(0, 12));
        this.data = data;
        this.account = account;
        this.jdAssistantController = jdAssistantController;
        this.aiIndexController = aiIndexController;
        setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));

        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);

        aiOutputArea.setEditable(false);
        aiOutputArea.setLineWrap(true);
        aiOutputArea.setWrapStyleWord(true);
        aiOutputArea.setText(I18n.t("mo.post.ai.initial"));

        add(buildFormPanel(), BorderLayout.NORTH);
        add(buildAiPanel(), BorderLayout.CENTER);

        previewButton.addActionListener(e -> preview());
        aiCheckButton.addActionListener(e -> reviewDraftWithAi());
        aiPolishButton.addActionListener(e -> polishDraftWithAi());
        submitButton.addActionListener(e -> submit());
    }

    private JPanel buildFormPanel() {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBorder(BorderFactory.createTitledBorder(I18n.t("mo.post.title")));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.fill = GridBagConstraints.HORIZONTAL;

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0;
        form.add(new JLabel(I18n.t("mo.post.label.title")), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1.0;
        form.add(titleField, c);

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 0;
        form.add(new JLabel(I18n.t("mo.post.label.skills")), c);

        c.gridx = 1;
        c.gridy = 1;
        c.weightx = 1.0;
        form.add(skillsField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.weightx = 0;
        form.add(new JLabel(I18n.t("mo.post.label.hours")), c);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 1.0;
        form.add(hoursField, c);

        c.gridx = 0;
        c.gridy = 3;
        c.weightx = 0;
        form.add(new JLabel(I18n.t("mo.post.label.desc")), c);

        JScrollPane descScroll = new JScrollPane(descArea);
        descScroll.setPreferredSize(new Dimension(420, 170));
        c.gridx = 1;
        c.gridy = 3;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        form.add(descScroll, c);

        JPanel actions = new JPanel();
        actions.add(previewButton);
        actions.add(aiCheckButton);
        actions.add(aiPolishButton);
        actions.add(submitButton);

        c.gridx = 1;
        c.gridy = 4;
        c.weightx = 1.0;
        c.weighty = 0;
        c.fill = GridBagConstraints.HORIZONTAL;
        form.add(actions, c);

        return form;
    }

    private JPanel buildAiPanel() {
        JPanel aiPanel = new JPanel(new BorderLayout(0, 8));
        aiPanel.setBorder(BorderFactory.createTitledBorder(I18n.t("mo.post.ai.panel")));

        aiPanel.add(aiStatusLabel, BorderLayout.NORTH);

        JScrollPane outputScroll = new JScrollPane(aiOutputArea);
        outputScroll.setPreferredSize(new Dimension(420, 220));
        aiPanel.add(outputScroll, BorderLayout.CENTER);

        return aiPanel;
    }

    private void preview() {
        JobDraft draft = validateDraft(true);
        if (draft == null) return;

        JTextArea previewArea = new JTextArea(buildPreviewText(draft));
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(500, 320));
        JOptionPane.showMessageDialog(this, scrollPane, I18n.t("common.preview"), JOptionPane.INFORMATION_MESSAGE);
    }

    private void reviewDraftWithAi() {
        JobDraft draft = validateDraft(true);
        if (draft == null || !ensureAiConfigured()) return;

        setAiBusy(true, I18n.t("mo.post.ai.checking"));
        aiOutputArea.setText(I18n.t("mo.post.ai.pending.review") + "\n\n");

        SwingWorker<JdQualityResultVo, String> worker = new SwingWorker<JdQualityResultVo, String>() {
            @Override
            protected JdQualityResultVo doInBackground() {
                return jdAssistantController.checkStream(draft.toDto(), new AiStreamListener() {
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
                    aiOutputArea.append(chunk);
                }
                aiOutputArea.setCaretPosition(aiOutputArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    JdQualityResultVo review = get();
                    aiStatusLabel.setText(I18n.t("mo.post.ai.check.done", Integer.valueOf(review.overallScore())));
                    aiOutputArea.setText(formatReview(review));
                    aiOutputArea.setCaretPosition(0);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showAiError(ex);
                } catch (ExecutionException ex) {
                    showAiError(rootCause(ex));
                } finally {
                    setAiBusy(false, aiStatusLabel.getText());
                }
            }
        };
        worker.execute();
    }

    private void polishDraftWithAi() {
        JobDraft draft = validateDraft(true);
        if (draft == null || !ensureAiConfigured()) return;

        setAiBusy(true, I18n.t("mo.post.ai.polishing"));
        aiOutputArea.setText(I18n.t("mo.post.ai.pending.polish") + "\n\n");

        SwingWorker<JdPolishResultVo, String> worker = new SwingWorker<JdPolishResultVo, String>() {
            @Override
            protected JdPolishResultVo doInBackground() {
                return jdAssistantController.polishStream(draft.toDto(), new AiStreamListener() {
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
                    aiOutputArea.append(chunk);
                }
                aiOutputArea.setCaretPosition(aiOutputArea.getDocument().getLength());
            }

            @Override
            protected void done() {
                try {
                    JdPolishResultVo polish = get();
                    aiStatusLabel.setText(I18n.t("mo.post.ai.polish.done"));
                    aiOutputArea.setText(formatPolish(polish));
                    aiOutputArea.setCaretPosition(0);

                    offerApplyPolish(polish);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    showAiError(ex);
                } catch (ExecutionException ex) {
                    showAiError(rootCause(ex));
                } finally {
                    setAiBusy(false, aiStatusLabel.getText());
                }
            }
        };
        worker.execute();
    }

    private void offerApplyPolish(JdPolishResultVo polish) {
        JTextArea previewArea = new JTextArea(formatPolish(polish));
        previewArea.setEditable(false);
        previewArea.setLineWrap(true);
        previewArea.setWrapStyleWord(true);
        previewArea.setCaretPosition(0);

        JScrollPane scrollPane = new JScrollPane(previewArea);
        scrollPane.setPreferredSize(new Dimension(520, 360));

        Object[] options = {
                I18n.t("mo.post.ai.apply.all"),
                I18n.t("mo.post.ai.apply.title"),
                I18n.t("mo.post.ai.apply.skills"),
                I18n.t("mo.post.ai.apply.description"),
                I18n.t("common.cancel")
        };
        int result = JOptionPane.showOptionDialog(
                this,
                scrollPane,
                I18n.t("mo.post.ai.polish.preview"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.INFORMATION_MESSAGE,
                null,
                options,
                options[0]);
        if (result == 0) {
            applyPolish(polish, true, true, true);
            aiStatusLabel.setText(I18n.t("mo.post.ai.applied"));
        } else if (result == 1) {
            applyPolish(polish, true, false, false);
            aiStatusLabel.setText(I18n.t("mo.post.ai.applied"));
        } else if (result == 2) {
            applyPolish(polish, false, true, false);
            aiStatusLabel.setText(I18n.t("mo.post.ai.applied"));
        } else if (result == 3) {
            applyPolish(polish, false, false, true);
            aiStatusLabel.setText(I18n.t("mo.post.ai.applied"));
        }
    }

    private void applyPolish(JdPolishResultVo polish, boolean applyTitle, boolean applySkills, boolean applyDescription) {
        if (applyTitle) titleField.setText(polish.title());
        if (applySkills) skillsField.setText(polish.requiredSkills());
        if (applyDescription) descArea.setText(polish.description());
    }

    private void submit() {
        JobDraft draft = validateDraft(true);
        if (draft == null) return;

        Job job = data.createJob(draft.title, draft.description, draft.requiredSkills, draft.hoursPerWeek, account);
        if (aiIndexController != null && job != null) aiIndexController.refreshJob(job.id());
        JOptionPane.showMessageDialog(this, I18n.t("mo.post.success"));
        resetForm();
    }

    private JobDraft validateDraft(boolean showMessage) {
        String title = titleField.getText().trim();
        String skills = skillsField.getText().trim();
        String hoursRaw = hoursField.getText().trim();
        String desc = descArea.getText().trim();

        if (title.isEmpty() || skills.isEmpty() || hoursRaw.isEmpty() || desc.isEmpty()) {
            if (showMessage) JOptionPane.showMessageDialog(this, I18n.t("msg.fields.required"));
            return null;
        }

        int hours;
        try {
            hours = Integer.parseInt(hoursRaw);
        } catch (NumberFormatException nfe) {
            if (showMessage) JOptionPane.showMessageDialog(this, I18n.t("mo.post.hours.nan"));
            return null;
        }

        if (hours <= 0) {
            if (showMessage) JOptionPane.showMessageDialog(this, I18n.t("mo.post.hours.invalid"));
            return null;
        }

        return new JobDraft(title, skills, hours, desc);
    }

    private boolean ensureAiConfigured() {
        if (jdAssistantController != null && jdAssistantController.isAiConfigured()) return true;

        JOptionPane.showMessageDialog(
                this,
                I18n.t("mo.post.ai.unconfigured",
                        jdAssistantController == null || jdAssistantController.configPath() == null
                                ? ""
                                : jdAssistantController.configPath().toAbsolutePath().toString()),
                I18n.t("mo.post.ai.panel"),
                JOptionPane.WARNING_MESSAGE);
        return false;
    }

    private void setAiBusy(boolean busy, String statusText) {
        aiCheckButton.setEnabled(!busy);
        aiPolishButton.setEnabled(!busy);
        previewButton.setEnabled(!busy);
        submitButton.setEnabled(!busy);
        aiStatusLabel.setText(statusText);
    }

    private void showAiError(Throwable ex) {
        String detail = ex == null ? I18n.t("msg.operation.failed") : ex.getMessage();
        if (detail == null || detail.trim().isEmpty()) detail = I18n.t("msg.operation.failed");

        aiStatusLabel.setText(I18n.t("mo.post.ai.failed"));
        aiOutputArea.setText(I18n.t("mo.post.ai.failed.detail", detail));
        aiOutputArea.setCaretPosition(0);

        JOptionPane.showMessageDialog(
                this,
                I18n.t("mo.post.ai.failed.detail", detail),
                I18n.t("mo.post.ai.panel"),
                JOptionPane.ERROR_MESSAGE);
    }

    private static Throwable rootCause(Throwable ex) {
        Throwable cur = ex;
        while (cur != null && cur.getCause() != null) {
            cur = cur.getCause();
        }
        return cur == null ? ex : cur;
    }

    private void resetForm() {
        titleField.setText("");
        skillsField.setText("");
        hoursField.setText("");
        descArea.setText("");
        aiStatusLabel.setText(I18n.t("mo.post.ai.idle"));
        aiOutputArea.setText(I18n.t("mo.post.ai.initial"));
    }

    private String buildPreviewText(JobDraft draft) {
        StringBuilder sb = new StringBuilder();
        sb.append("Title: ").append(draft.title).append("\n");
        sb.append("Required skills: ").append(draft.requiredSkills).append("\n");
        sb.append("Hours/week: ").append(draft.hoursPerWeek).append("\n\n");
        sb.append(draft.description);
        return sb.toString();
    }

    private String formatReview(JdQualityResultVo review) {
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("mo.post.ai.review.score")).append(": ").append(review.overallScore()).append("/100").append("\n\n");
        sb.append(I18n.t("mo.post.ai.review.summary")).append(":\n").append(nonBlank(review.summary())).append("\n\n");
        sb.append(I18n.t("mo.post.ai.review.issues")).append(":\n").append(formatIssues(review.issues())).append("\n\n");
        sb.append(I18n.t("mo.post.ai.review.suggestions")).append(":\n").append(formatList(review.suggestions()));
        return sb.toString().trim();
    }

    private String formatPolish(JdPolishResultVo polish) {
        StringBuilder sb = new StringBuilder();
        sb.append(I18n.t("mo.post.ai.polish.preview")).append("\n\n");
        sb.append(I18n.t("mo.post.label.title")).append(" ").append(polish.title()).append("\n\n");
        sb.append(I18n.t("mo.post.label.skills")).append(" ").append(polish.requiredSkills()).append("\n\n");
        sb.append(I18n.t("mo.post.label.desc")).append("\n").append(polish.description()).append("\n\n");
        sb.append(I18n.t("mo.post.ai.polish.changes")).append(":\n").append(formatList(polish.changeSummary()));
        return sb.toString().trim();
    }

    private static String formatList(List<String> items) {
        if (items == null || items.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (String item : items) {
            if (item == null || item.trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append("- ").append(item.trim());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private static String formatIssues(List<JdQualityIssueVo> issues) {
        if (issues == null || issues.isEmpty()) return "-";
        StringBuilder sb = new StringBuilder();
        for (JdQualityIssueVo issue : issues) {
            if (issue == null || issue.message().trim().isEmpty()) continue;
            if (sb.length() > 0) sb.append("\n");
            sb.append("- [").append(issue.dimension()).append("/").append(issue.severity()).append("] ").append(issue.message());
        }
        return sb.length() == 0 ? "-" : sb.toString();
    }

    private static String nonBlank(String text) {
        return text == null || text.trim().isEmpty() ? "-" : text.trim();
    }

    private static final class JobDraft {
        private final String title;
        private final String requiredSkills;
        private final int hoursPerWeek;
        private final String description;

        private JobDraft(String title, String requiredSkills, int hoursPerWeek, String description) {
            this.title = title;
            this.requiredSkills = requiredSkills;
            this.hoursPerWeek = hoursPerWeek;
            this.description = description;
        }

        private JobDraftDto toDto() {
            return new JobDraftDto(title, description, requiredSkills, hoursPerWeek);
        }
    }
}
