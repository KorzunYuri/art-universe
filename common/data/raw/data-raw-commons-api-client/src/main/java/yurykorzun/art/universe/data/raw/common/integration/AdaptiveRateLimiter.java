package yurykorzun.art.universe.data.raw.common.integration;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Adaptive rate limiter with exponential backoff and linear recovery.
 * <p>
 * On success: decreases the delay by a recovery step (clamped to {@code minDelayMs}).
 * On 429/rate-limit: multiplies the delay by {@code backoffMultiplier} (clamped to {@code maxDelayMs}).
 * <p>
 * All tuning parameters can be updated at runtime via setters, enabling live adjustment
 * from an external configuration source without restarting the application.
 */
@Slf4j
public class AdaptiveRateLimiter {

    private volatile long minDelayMs;
    private volatile long maxDelayMs;
    private volatile double backoffMultiplier;
    private final AtomicLong currentDelayMs;

    public AdaptiveRateLimiter(long minDelayMs, long maxDelayMs, double backoffMultiplier) {
        this.minDelayMs = minDelayMs;
        this.maxDelayMs = maxDelayMs;
        this.backoffMultiplier = backoffMultiplier;
        this.currentDelayMs = new AtomicLong(minDelayMs);
    }

    public void acquire() throws InterruptedException {
        Thread.sleep(currentDelayMs.get());
    }

    public void recordSuccess() {
        long min = this.minDelayMs;
        long recoveryStep = Math.max(50L, min / 2);
        long updated = currentDelayMs.updateAndGet(cur -> Math.max(min, cur - recoveryStep));
        log.debug("Rate limiter recovering: {}ms between calls", updated);
    }

    public void record429() {
        long max = this.maxDelayMs;
        double multiplier = this.backoffMultiplier;
        long updated = currentDelayMs.updateAndGet(cur -> Math.min(max, (long) (cur * multiplier)));
        log.warn("Rate limited (429). Backing off to {}ms between calls", updated);
    }

    public void setMinDelayMs(long minDelayMs) {
        this.minDelayMs = minDelayMs;
        log.info("Rate limiter minDelayMs updated to {}", minDelayMs);
    }

    public void setMaxDelayMs(long maxDelayMs) {
        this.maxDelayMs = maxDelayMs;
        log.info("Rate limiter maxDelayMs updated to {}", maxDelayMs);
    }

    public void setBackoffMultiplier(double backoffMultiplier) {
        this.backoffMultiplier = backoffMultiplier;
        log.info("Rate limiter backoffMultiplier updated to {}", backoffMultiplier);
    }
}
