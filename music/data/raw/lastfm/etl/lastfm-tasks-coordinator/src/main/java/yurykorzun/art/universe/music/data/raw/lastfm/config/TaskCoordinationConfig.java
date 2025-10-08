package yurykorzun.art.universe.music.data.raw.lastfm.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
public class TaskCoordinationConfig {

    @Bean("tasksExecutor")
    @ConditionalOnMissingBean(name = {"tasksExecutor"})
    public ExecutorService tasksExecutor() {
        return Executors.newFixedThreadPool(5, r -> {
            Thread t = new Thread(r);
            t.setName("tasks-" + t.threadId());
            t.setDaemon(true);
            return t;
        });
    }

}
