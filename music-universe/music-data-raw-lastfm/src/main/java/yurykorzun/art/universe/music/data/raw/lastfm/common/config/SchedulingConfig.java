package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service.DbMaintenanceCoordinator;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {

    @Value("${scheduling.lastfm.tasks.poolSize}")
    private int poolSize;

    @Bean("dataCollectionScheduler")
    public TaskScheduler coordinatingTaskScheduler(DbMaintenanceCoordinator coordinator) {
        ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
        pool.setPoolSize(poolSize);
        pool.setThreadNamePrefix("schdlr-");
        pool.initialize();
        return new CoordinatingTaskScheduler(pool, coordinator);
    }

    @Bean("maintenanceScheduler")
    public TaskScheduler maintenanceScheduler() {
        ThreadPoolTaskScheduler pool = new ThreadPoolTaskScheduler();
        pool.setPoolSize(1);
        pool.setThreadNamePrefix("mtnc-");
        pool.initialize();
        return pool;
    }
}
