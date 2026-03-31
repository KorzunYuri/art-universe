package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.Map;

public class LlmClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(LlmClientRegistry.class);

    private final Map<AnalysisMode, LlmClient> clients;

    public LlmClientRegistry(Map<AnalysisMode, LlmClient> clients) {
        this.clients = clients;
        log.info("LLM client registry initialized with modes: {}", clients.keySet());
    }

    public LlmClient getClient(AnalysisMode mode) {
        LlmClient client = clients.get(mode);
        if (client == null) {
            throw new IllegalStateException("No LLM client configured for analysis mode: " + mode.getName());
        }
        return client;
    }
}
