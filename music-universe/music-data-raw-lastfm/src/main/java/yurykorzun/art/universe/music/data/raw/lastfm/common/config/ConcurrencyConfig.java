package yurykorzun.art.universe.music.data.raw.lastfm.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class ConcurrencyConfig {

    @Bean("tasksExecutor")
    public ExecutorService tasksExecutor() {
        return Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r);
            t.setName("tasks-" + t.getId());
            t.setDaemon(true);
            return t;
        });
    }
}
