package yurykorzun.art.universe.music.data.semantic.analyzer.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmClient;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.LlmResponse;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.ClientSelection;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmClientCircuitBreaker;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmClientState;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureClassifier;
import yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience.LlmFailureType;
import yurykorzun.art.universe.music.data.semantic.model.AnalysisMode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Routes analysis modes to LLM clients with circuit-breaker-based fallback.
 * <p>
 * Each mode maps to an ordered list of client names. The first available
 * (healthy or probe-eligible) client is returned. Failures are reported back
 * so the circuit breaker can trip/disable clients as needed.
 */
public class LlmClientRegistry {

    private static final Logger log = LoggerFactory.getLogger(LlmClientRegistry.class);

    private final Map<String, LlmClient> clients;
    private final Map<String, LlmClientCircuitBreaker> circuitBreakers;
    private final Map<String, LlmFailureClassifier> classifiers;
    private final Map<AnalysisMode, List<String>> modeClientOrder;

    public LlmClientRegistry(
        Map<String, LlmClient> clients,
        Map<String, LlmClientCircuitBreaker> circuitBreakers,
        Map<String, LlmFailureClassifier> classifiers,
        Map<AnalysisMode, List<String>> modeClientOrder
    ) {
        this.clients = clients;
        this.circuitBreakers = circuitBreakers;
        this.classifiers = classifiers;
        this.modeClientOrder = modeClientOrder;
        log.info("LLM client registry initialized: modes={}, clients={}",
            modeClientOrder.keySet(), clients.keySet());
    }

    /**
     * Returns the first available client for the given mode, or {@code null}
     * if all clients in the priority list are cooling/disabled.
     */
    public ClientSelection acquireClient(AnalysisMode mode) {
        List<String> clientNames = modeClientOrder.get(mode);
        if (clientNames == null || clientNames.isEmpty()) {
            throw new IllegalStateException(
                "No LLM clients configured for analysis mode: " + mode.getName());
        }
        for (String name : clientNames) {
            LlmClientCircuitBreaker breaker = circuitBreakers.get(name);
            if (breaker.isAvailable()) {
                LlmClientState state = breaker.getState();
                if (state == LlmClientState.COOLING) {
                    log.info("Probing client '{}' after cooldown for mode {}", name, mode.getName());
                }
                return new ClientSelection(clients.get(name), name);
            }
        }
        log.warn("All LLM clients exhausted for mode {}: {}",
            mode.getName(), clientNames);
        return null;
    }

    public void reportSuccess(String clientName) {
        circuitBreakers.get(clientName).recordSuccess();
    }

    /**
     * Classifies the failure (ban vs rate-limit vs other) and updates the
     * circuit breaker accordingly. Returns the classified failure type.
     */
    public LlmFailureType reportFailure(String clientName, LlmResponse response) {
        LlmFailureClassifier classifier = classifiers.get(clientName);
        LlmFailureType type = classifier.classify(
            response.getHttpStatus(), response.getRawErrorBody());

        LlmClientCircuitBreaker breaker = circuitBreakers.get(clientName);
        switch (type) {
            case BANNED -> breaker.recordBan(response.getErrorMessage());
            case RATE_LIMITED -> breaker.recordRateLimit();
            default -> {
                // Server errors / network errors: treat like rate limit for circuit-breaker
                // purposes so we can fallback after repeated failures
                breaker.recordRateLimit();
            }
        }
        return type;
    }

    public void forceDisable(String clientName, String reason) {
        LlmClientCircuitBreaker breaker = circuitBreakers.get(clientName);
        if (breaker == null) {
            throw new IllegalArgumentException("Unknown client: " + clientName);
        }
        breaker.forceDisable(reason);
    }

    public void forceEnable(String clientName) {
        LlmClientCircuitBreaker breaker = circuitBreakers.get(clientName);
        if (breaker == null) {
            throw new IllegalArgumentException("Unknown client: " + clientName);
        }
        breaker.forceEnable();
    }

    /**
     * Returns a snapshot of all clients' current state for monitoring.
     */
    public Map<String, ClientStatusInfo> getClientStatuses() {
        Map<String, ClientStatusInfo> result = new LinkedHashMap<>();
        for (Map.Entry<String, LlmClientCircuitBreaker> entry : circuitBreakers.entrySet()) {
            LlmClientCircuitBreaker breaker = entry.getValue();
            result.put(entry.getKey(), new ClientStatusInfo(
                breaker.getState(),
                breaker.getDisableReason(),
                breaker.getCooldownUntil()
            ));
        }
        return result;
    }

    public record ClientStatusInfo(
        LlmClientState state,
        String disableReason,
        java.time.Instant cooldownUntil
    ) {
    }
}
