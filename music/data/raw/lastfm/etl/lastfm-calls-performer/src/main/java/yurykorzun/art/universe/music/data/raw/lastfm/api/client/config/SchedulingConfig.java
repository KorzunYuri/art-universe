package yurykorzun.art.universe.music.data.raw.lastfm.api.client.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

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
        scheduler.setThreadNamePrefix("lastfm-calls-performer-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(taskAwaitTerminationSeconds);
        scheduler.initialize();
        return scheduler;
    }
}
