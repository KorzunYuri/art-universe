package yurykorzun.art.universe.music.data.semantic.analyzer.llm.resilience;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Per-client circuit breaker that tracks health and manages cooling/disabled states.
 * <p>
 * State transitions:
 * <pre>
 *   HEALTHY ──(consecutive 429s >= threshold)──> COOLING
 *   HEALTHY ──(ban detected)──────────────────> DISABLED
 *   COOLING ──(cooldown expires, probe succeeds)──> HEALTHY
 *   COOLING ──(cooldown expires, probe fails)────> COOLING (longer cooldown)
 *   DISABLED ──(manual force-enable)──────────> HEALTHY
 *   Any     ──(manual force-disable)──────────> DISABLED
 * </pre>
 * Thread-safe: all mutable state uses atomic references.
 */
public class LlmClientCircuitBreaker {

    private static final Logger log = LoggerFactory.getLogger(LlmClientCircuitBreaker.class);

    private final String clientName;
    private final int rateLimitThreshold;
    private final Duration initialCooldown;
    private final Duration maxCooldown;
    private final double cooldownMultiplier;

    private final AtomicReference<LlmClientState> state = new AtomicReference<>(LlmClientState.HEALTHY);
    private final AtomicReference<Instant> cooldownUntil = new AtomicReference<>(Instant.MIN);
    private final AtomicReference<Duration> currentCooldown = new AtomicReference<>();
    private final AtomicInteger consecutiveRateLimits = new AtomicInteger(0);
    private final AtomicReference<String> disableReason = new AtomicReference<>();

    public LlmClientCircuitBreaker(
        String clientName,
        int rateLimitThreshold,
        Duration initialCooldown,
        Duration maxCooldown,
        double cooldownMultiplier
    ) {
        this.clientName = clientName;
        this.rateLimitThreshold = rateLimitThreshold;
        this.initialCooldown = initialCooldown;
        this.maxCooldown = maxCooldown;
        this.cooldownMultiplier = cooldownMultiplier;
        this.currentCooldown.set(initialCooldown);
    }

    /**
     * Returns {@code true} if this client can accept a request right now.
     * A COOLING client becomes available once its cooldown expires (probe attempt).
     */
    public boolean isAvailable() {
        return switch (state.get()) {
            case HEALTHY -> true;
            case COOLING -> Instant.now().isAfter(cooldownUntil.get());
            case DISABLED -> false;
        };
    }

    public void recordSuccess() {
        if (state.get() == LlmClientState.COOLING) {
            log.info("Client '{}' probe succeeded — restored to HEALTHY", clientName);
        }
        state.set(LlmClientState.HEALTHY);
        consecutiveRateLimits.set(0);
        currentCooldown.set(initialCooldown);
    }

    public void recordRateLimit() {
        int count = consecutiveRateLimits.incrementAndGet();
        if (count >= rateLimitThreshold || state.get() == LlmClientState.COOLING) {
            enterCooling();
        }
    }

    public void recordBan(String reason) {
        state.set(LlmClientState.DISABLED);
        disableReason.set(reason);
        log.error("Client '{}' DISABLED — ban detected: {}", clientName, reason);
    }

    public void forceDisable(String reason) {
        state.set(LlmClientState.DISABLED);
        disableReason.set(reason);
        log.warn("Client '{}' manually DISABLED: {}", clientName, reason);
    }

    public void forceEnable() {
        state.set(LlmClientState.HEALTHY);
        consecutiveRateLimits.set(0);
        currentCooldown.set(initialCooldown);
        disableReason.set(null);
        log.info("Client '{}' manually re-enabled — HEALTHY", clientName);
    }

    public LlmClientState getState() {
        return state.get();
    }

    public String getDisableReason() {
        return disableReason.get();
    }

    public Instant getCooldownUntil() {
        return cooldownUntil.get();
    }

    private void enterCooling() {
        Duration cooldown = currentCooldown.get();
        Instant until = Instant.now().plus(cooldown);
        cooldownUntil.set(until);
        state.set(LlmClientState.COOLING);

        // Grow cooldown for next time, capped at max
        long nextMs = Math.min((long) (cooldown.toMillis() * cooldownMultiplier), maxCooldown.toMillis());
        currentCooldown.set(Duration.ofMillis(nextMs));

        log.warn("Client '{}' entered COOLING state for {} (next cooldown: {})",
            clientName, cooldown, Duration.ofMillis(nextMs));
    }
}
