package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.openai.OpenAiLlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmClientCircuitBreaker;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureClassifier;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureClassifier.CompiledBanPattern;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ProviderErrorAnalyzer;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Registry bean factory. Reads {@code llm.yml}, instantiates one
 * {@link OpenAiLlmClient} per configured entry (all three supported providers
 * — openai, gemini, groq — share the OpenAI-compatible chat-completions wire
 * format), pairs each client with a circuit breaker and a per-provider
 * {@link LlmFailureClassifier}, and hands the bundle to
 * {@link LlmClientRegistry}.
 */
@Configuration
public class LlmClientConfig {

    private static final Logger log = LoggerFactory.getLogger(LlmClientConfig.class);

    /** Providers that speak OpenAI's {@code /chat/completions} wire format. */
    private static final Set<String> OPENAI_COMPATIBLE_PROVIDERS = Set.of("openai", "gemini", "groq");

    @Bean
    public LlmClientRegistry llmClientRegistry(
        LlmClientProperties clientProps,
        AnalysisModeProperties modeProps,
        ObjectMapper objectMapper,
        List<ProviderErrorAnalyzer> providerAnalyzers
    ) {
        Map<String, ProviderErrorAnalyzer> analyzersByProvider = providerAnalyzers.stream()
            .collect(Collectors.toMap(ProviderErrorAnalyzer::provider, Function.identity()));

        Map<String, LlmClient> namedClients = new HashMap<>();
        Map<String, LlmClientCircuitBreaker> circuitBreakers = new HashMap<>();
        Map<String, LlmFailureClassifier> classifiers = new HashMap<>();

        for (Map.Entry<String, LlmClientProperties.ClientConfig> entry : clientProps.getClients().entrySet()) {
            String name = entry.getKey();
            LlmClientProperties.ClientConfig cfg = entry.getValue();

            ProviderErrorAnalyzer analyzer = analyzersByProvider.get(cfg.getProvider());
            if (analyzer == null) {
                throw new IllegalStateException(
                    "No ProviderErrorAnalyzer registered for provider '" + cfg.getProvider()
                        + "' (client '" + name + "'). Available: " + analyzersByProvider.keySet());
            }

            namedClients.put(name, createClient(objectMapper, cfg));
            circuitBreakers.put(name, createCircuitBreaker(name, cfg.getCircuitBreaker()));
            classifiers.put(name, createClassifier(name, analyzer, cfg.getBanPatterns()));

            log.info("Created LLM client '{}': provider={}, model={}, temperature={}, ban-patterns={}",
                    name, cfg.getProvider(), cfg.getModel(), cfg.getTemperature(),
                    cfg.getBanPatterns() == null ? 0 : cfg.getBanPatterns().size());
        }

        Map<AnalysisMode, List<String>> modeClientOrder = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : modeProps.getModes().entrySet()) {
            AnalysisMode mode = AnalysisMode.fromString(entry.getKey());
            List<String> clientNames = entry.getValue();
            for (String clientName : clientNames) {
                if (!namedClients.containsKey(clientName)) {
                    throw new IllegalStateException(
                            "Analysis mode '" + mode.getName() + "' references unknown client '"
                                    + clientName + "'. Available: " + namedClients.keySet());
                }
            }
            modeClientOrder.put(mode, clientNames);
            log.info("Mode '{}' client priority: {}", mode.getName(), clientNames);
        }

        return new LlmClientRegistry(namedClients, circuitBreakers, classifiers, modeClientOrder);
    }

    private LlmClient createClient(ObjectMapper objectMapper, LlmClientProperties.ClientConfig cfg) {
        AdaptiveRateLimiter rateLimiter = new AdaptiveRateLimiter(
            cfg.getRateLimit().getMinDelayMs(),
            cfg.getRateLimit().getMaxDelayMs(),
            cfg.getRateLimit().getBackoffMultiplier()
        );
        if (OPENAI_COMPATIBLE_PROVIDERS.contains(cfg.getProvider())) {
            return new OpenAiLlmClient(
                objectMapper,
                cfg.getProvider(),
                cfg.getApiKey(),
                cfg.getBaseUrl(),
                cfg.getModel(),
                cfg.getTemperature(),
                cfg.getConnectTimeout(),
                cfg.getReadTimeout(),
                rateLimiter
            );
        }
        throw new IllegalArgumentException("Unknown LLM provider: " + cfg.getProvider());
    }

    private LlmClientCircuitBreaker createCircuitBreaker(
        String clientName,
        LlmClientProperties.CircuitBreakerConfig cfg
    ) {
        return new LlmClientCircuitBreaker(
            clientName,
            cfg.getRateLimitThreshold(),
            cfg.getInitialCooldown(),
            cfg.getMaxCooldown(),
            cfg.getCooldownMultiplier()
        );
    }

    private LlmFailureClassifier createClassifier(
        String clientName,
        ProviderErrorAnalyzer analyzer,
        List<LlmClientProperties.BanPatternConfig> patternConfigs
    ) {
        List<CompiledBanPattern> compiled = new ArrayList<>();
        if (patternConfigs != null) {
            for (LlmClientProperties.BanPatternConfig pc : patternConfigs) {
                Pattern bodyRegex = pc.getBodyPattern() == null
                    ? null
                    : Pattern.compile(pc.getBodyPattern(), Pattern.CASE_INSENSITIVE);
                compiled.add(new CompiledBanPattern(pc.getStatus(), bodyRegex));
            }
        }
        return new LlmFailureClassifier(clientName, analyzer, compiled);
    }
}
