package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class AdaptiveRateLimiter {

    private final long minDelayMs;
    private final long maxDelayMs;
    private final double backoffMultiplier;
    private final long recoveryStepMs;

    private final AtomicLong currentDelayMs;

    public AdaptiveRateLimiter(
            @Value("${spotify.tasks.calls-perform.rate-limiter.min-delay-ms}") long minDelayMs,
            @Value("${spotify.tasks.calls-perform.rate-limiter.max-delay-ms}") long maxDelayMs,
            @Value("${spotify.tasks.calls-perform.rate-limiter.initial-delay-ms}") long initialDelayMs,
            @Value("${spotify.tasks.calls-perform.rate-limiter.backoff-multiplier}") double backoffMultiplier,
            @Value("${spotify.tasks.calls-perform.rate-limiter.recovery-step-ms}") long recoveryStepMs
    ) {
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.recoveryStepMs = recoveryStepMs;
        this.currentDelayMs = new AtomicLong(initialDelayMs);
    }

    public void acquire() throws InterruptedException {
        Thread.sleep(currentDelayMs.get());
    }

    public void recordSuccess() {
        long updated = currentDelayMs.updateAndGet(cur -> Math.max(minDelayMs, cur - recoveryStepMs));
        log.debug("Rate limiter recovering: {}ms between calls", updated);
    }

    public void record429() {
        long updated = currentDelayMs.updateAndGet(cur -> Math.min(maxDelayMs, (long) (cur * backoffMultiplier)));
        log.warn("Rate limited (429). Backing off to {}ms between calls", updated);
    }
}
