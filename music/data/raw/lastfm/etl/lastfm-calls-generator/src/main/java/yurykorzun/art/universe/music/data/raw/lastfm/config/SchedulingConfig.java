package yurykorzun.art.universe.music.data.raw.lastfm.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig implements SchedulingConfigurer {

    @Value("${lastfm.calls.generate.scheduling.pool.size}")
    private Integer taskSchedulerPoolSize;

    @Value("${lastfm.calls.generate.scheduling.pool.awaitTerminationSeconds}")
    private Integer taskAwaitTerminationSeconds;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        scheduler.setPoolSize(taskSchedulerPoolSize);
        scheduler.setThreadNamePrefix("lastfm-calls-generator-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(taskAwaitTerminationSeconds);
        scheduler.initialize();
        taskRegistrar.setTaskScheduler(scheduler);
    }
}
