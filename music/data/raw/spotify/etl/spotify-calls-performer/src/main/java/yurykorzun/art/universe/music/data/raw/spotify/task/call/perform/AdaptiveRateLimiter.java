package yurykorzun.art.universe.music.data.raw.spotify.task.call.perform;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyPerformerProperty;

import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
public class AdaptiveRateLimiter {

    private final ConfigPropertyHolder configPropertyHolder;

    private long minDelayMs;
    private long maxDelayMs;
    private double backoffMultiplier;
    private long recoveryStepMs;
    private AtomicLong currentDelayMs;

    public AdaptiveRateLimiter(ConfigPropertyHolder configPropertyHolder) {
        this.configPropertyHolder = configPropertyHolder;
    }

    @PostConstruct
    public void init() {
        this.minDelayMs = configPropertyHolder.getInt(SpotifyPerformerProperty.RATE_LIMITER_MIN_DELAY_MS);
        this.maxDelayMs = configPropertyHolder.getInt(SpotifyPerformerProperty.RATE_LIMITER_MAX_DELAY_MS);
        this.backoffMultiplier = configPropertyHolder.getDecimal(SpotifyPerformerProperty.RATE_LIMITER_BACKOFF_MULTIPLIER).doubleValue();
        this.recoveryStepMs = Math.max(50L, minDelayMs / 2);
        this.currentDelayMs = new AtomicLong(minDelayMs);
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
