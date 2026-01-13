package yurykorzun.art.universe.music.data.raw.lastfm.api.client.scheduling;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGeneratorsRegistry;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallGenerationScheduler {

    public LastfmApiCallGenerationScheduler() {
    }

    @Scheduled(
        fixedDelayString = "${lastfm.scheduling.calls-generate.fixed-delay-secs}",
        timeUnit = TimeUnit.SECONDS
    )
    public void generateApiCalls() {
        log.info("start API calls generation");
        LastfmApiCallGeneratorsRegistry.getRegistry()
            .forEach((apiCallType, generator) -> {
                log.info("start API calls generation for method {}", generator.getApiCallType().getMethod());
                generator.createApiCalls();
                log.info("finished API calls generation for method {}", generator.getApiCallType().getMethod());
            });
        log.info("finished API calls generation");
    }
}
