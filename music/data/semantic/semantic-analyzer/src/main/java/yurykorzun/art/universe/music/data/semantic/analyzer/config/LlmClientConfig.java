package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.openai.OpenAiLlmClient;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LlmClientConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

    @Bean
    public LlmClientRegistry llmClientRegistry(
        LlmClientProperties clientProps,
        AnalysisModeProperties modeProps,
        ObjectMapper objectMapper
    ) {
        // Build named client instances
        Map<String, LlmClient> namedClients = new HashMap<>();
        for (Map.Entry<String, LlmClientProperties.ClientConfig> entry : clientProps.getClients().entrySet()) {
            String name = entry.getKey();
            LlmClientProperties.ClientConfig cfg = entry.getValue();
            LlmClient client = createClient(objectMapper, cfg);
            namedClients.put(name, client);
            log.info("Created LLM client '{}': provider={}, model={}, temperature={}",
                    name, cfg.getProvider(), cfg.getModel(), cfg.getTemperature());
        }

        // Map analysis modes to client instances
        Map<AnalysisMode, LlmClient> modeClients = new HashMap<>();
        for (Map.Entry<String, String> entry : modeProps.getModes().entrySet()) {
            AnalysisMode mode = AnalysisMode.fromString(entry.getKey());
            String clientName = entry.getValue();
            LlmClient client = namedClients.get(clientName);
            if (client == null) {
                throw new IllegalStateException(
                        "Analysis mode '" + mode.getName() + "' references unknown client '" + clientName
                                + "'. Available clients: " + namedClients.keySet());
            }
            modeClients.put(mode, client);
        }

        return new LlmClientRegistry(modeClients);
    }

    private LlmClient createClient(ObjectMapper objectMapper, LlmClientProperties.ClientConfig cfg) {
        AdaptiveRateLimiter rateLimiter = new AdaptiveRateLimiter(
            cfg.getRateLimit().getMinDelayMs(),
            cfg.getRateLimit().getMaxDelayMs(),
            cfg.getRateLimit().getBackoffMultiplier()
        );
        return switch (cfg.getProvider()) {
            case "openai" -> new OpenAiLlmClient(
                objectMapper,
                cfg.getApiKey(),
                cfg.getBaseUrl(),
                cfg.getModel(),
                cfg.getTemperature(),
                cfg.getConnectTimeout(),
                cfg.getReadTimeout(),
                rateLimiter
            );
            default -> throw new IllegalArgumentException("Unknown LLM provider: " + cfg.getProvider());
        };
    }
}
