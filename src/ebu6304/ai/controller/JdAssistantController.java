package ebu6304.ai.controller;

import java.nio.file.Path;

import ebu6304.ai.client.AiClientFactory;
import ebu6304.ai.client.AiStreamListener;
import ebu6304.ai.dto.JobDraftDto;
import ebu6304.ai.vo.JdPolishResultVo;
import ebu6304.ai.vo.JdQualityResultVo;
import ebu6304.ai.workflow.JdOptimizationWorkflow;

public final class JdAssistantController {
    private final AiClientFactory aiClientFactory;
    private final JdOptimizationWorkflow workflow;

    public JdAssistantController(AiClientFactory aiClientFactory, JdOptimizationWorkflow workflow) {
        this.aiClientFactory = aiClientFactory;
        this.workflow = workflow;
    }

    public boolean isAiConfigured() {
        return aiClientFactory != null && aiClientFactory.isConfigured();
    }

    public Path configPath() {
        return aiClientFactory == null ? null : aiClientFactory.configPath();
    }

    public JdQualityResultVo check(JobDraftDto draft) {
        return workflow.review(draft);
    }

    public JdQualityResultVo checkStream(JobDraftDto draft, AiStreamListener listener) {
        return workflow.review(draft, listener);
    }

    public JdPolishResultVo polish(JobDraftDto draft) {
        return workflow.polish(draft);
    }

    public JdPolishResultVo polishStream(JobDraftDto draft, AiStreamListener listener) {
        return workflow.polish(draft, listener);
    }
}
