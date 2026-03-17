package yurykorzun.art.universe.music.data.raw.spotify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.application.StagingApplicationScheduler;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(2);
        scheduler.setThreadNamePrefix("spotify-staging-applicator-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(90);
        scheduler.initialize();
        return scheduler;
    }

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop iterationsNotificationLoop(
        StagingApplicationScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            SpotifyConstants.NOTIFY_ITERATIONS_SEALED,
            scheduler::executeWork,
            () -> configPropertyHolder.getInt(SpotifyApplicatorProperty.STAGING_APPLY_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
