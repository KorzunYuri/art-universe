package yurykorzun.art.universe.music.data.raw.spotify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.data.raw.common.integration.AdaptiveRateLimiter;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.task.call.perform.SpotifyApiCallExecutionScheduler;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {

    @Bean
    public AdaptiveRateLimiter adaptiveRateLimiter(ConfigPropertyHolder configPropertyHolder) {
        AdaptiveRateLimiter limiter = new AdaptiveRateLimiter(
            configPropertyHolder.getInt(SpotifyPerformerProperty.RATE_LIMITER_MIN_DELAY_MS),
            configPropertyHolder.getInt(SpotifyPerformerProperty.RATE_LIMITER_MAX_DELAY_MS),
            configPropertyHolder.getDecimal(SpotifyPerformerProperty.RATE_LIMITER_BACKOFF_MULTIPLIER).doubleValue()
        );

        configPropertyHolder.onChange(SpotifyPerformerProperty.RATE_LIMITER_MIN_DELAY_MS,
            v -> limiter.setMinDelayMs(((Integer) v).longValue()));
        configPropertyHolder.onChange(SpotifyPerformerProperty.RATE_LIMITER_MAX_DELAY_MS,
            v -> limiter.setMaxDelayMs(((Integer) v).longValue()));
        configPropertyHolder.onChange(SpotifyPerformerProperty.RATE_LIMITER_BACKOFF_MULTIPLIER,
            v -> limiter.setBackoffMultiplier(((java.math.BigDecimal) v).doubleValue()));

        return limiter;
    }

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop callsNotificationLoop(
        SpotifyApiCallExecutionScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            SpotifyConstants.NOTIFY_CALLS_READY,
            scheduler::executeWork,
            () -> configPropertyHolder.getInt(SpotifyPerformerProperty.SCHEDULE_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
