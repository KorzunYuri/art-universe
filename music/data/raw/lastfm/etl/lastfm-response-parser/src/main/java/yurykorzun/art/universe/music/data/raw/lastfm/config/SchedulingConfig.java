package yurykorzun.art.universe.music.data.raw.lastfm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessingScheduler;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {

    @Value("${lastfm.scheduling.pool.size}")
    private Integer taskSchedulerPoolSize;

    @Value("${lastfm.scheduling.pool.await-termination-secs}")
    private Integer taskAwaitTerminationSeconds;

    @Bean(destroyMethod = "shutdown")
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(taskSchedulerPoolSize);
        scheduler.setThreadNamePrefix("lastfm-response-parser-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(taskAwaitTerminationSeconds);
        scheduler.initialize();
        return scheduler;
    }

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop responsesNotificationLoop(
        LastfmApiResponseProcessingScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            LastfmConstants.NOTIFY_RESPONSES_READY,
            scheduler::executeWork,
            () -> configPropertyHolder.getInt(LastfmParserProperty.SCHEDULE_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
