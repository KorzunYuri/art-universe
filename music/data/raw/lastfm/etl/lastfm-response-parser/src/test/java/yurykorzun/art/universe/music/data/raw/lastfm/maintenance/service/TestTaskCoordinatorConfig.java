package yurykorzun.art.universe.music.data.raw.lastfm.maintenance.service;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.Executors;

@TestConfiguration
public class TestTaskCoordinatorConfig {

    @Bean
    @Primary
    public TaskCoordinator taskCoordinator() {
        return new TaskCoordinator(Executors.newSingleThreadExecutor());
    }
}
