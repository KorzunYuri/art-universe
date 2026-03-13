package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class LastfmApiCallGenerationScheduler {

    private final ConfigPropertyHolder configPropertyHolder;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "lastfm-generator-scheduler");
        t.setDaemon(true);
        return t;
    });

    public LastfmApiCallGenerationScheduler(ConfigPropertyHolder configPropertyHolder) {
        this.configPropertyHolder = configPropertyHolder;
    }

    @PostConstruct
    public void start() {
        scheduleNext();
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
    }

    private void scheduleNext() {
        int delaySeconds = configPropertyHolder.getInt(LastfmGeneratorProperty.SCHEDULE_DELAY_SECS);
        scheduler.schedule(this::run, delaySeconds, TimeUnit.SECONDS);
    }

    private void run() {
        try {
            log.info("start API calls generation");
            LastfmApiCallGeneratorsRegistry.getRegistry()
                .forEach((apiCallType, generator) -> {
                    if (isGenerationEnabled(apiCallType)) {
                        log.info("start API calls generation for method {}", generator.getApiCallType().getMethod());
                        generator.createApiCalls();
                        log.info("finished API calls generation for method {}", generator.getApiCallType().getMethod());
                    } else {
                        log.debug("Generation disabled for method {}", apiCallType.getMethod());
                    }
                });
            log.info("finished API calls generation");
        } catch (Exception e) {
            log.error("Error during API calls generation: {}", e.getMessage(), e);
        } finally {
            scheduleNext();
        }
    }

    private boolean isGenerationEnabled(yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType callType) {
        LastfmGeneratorProperty enableProp = switch (callType) {
            case ARTIST_GET_INFO -> LastfmGeneratorProperty.GENERATE_ARTIST_GET_INFO;
            case ARTIST_GET_SIMILAR -> LastfmGeneratorProperty.GENERATE_ARTIST_GET_SIMILAR;
            case ARTIST_TOP_ALBUMS -> LastfmGeneratorProperty.GENERATE_ARTIST_TOP_ALBUMS;
            case ARTIST_TOP_TRACKS -> LastfmGeneratorProperty.GENERATE_ARTIST_TOP_TRACKS;
            case ARTIST_TOP_TAGS -> LastfmGeneratorProperty.GENERATE_ARTIST_TOP_TAGS;
            case ALBUM_GET_INFO -> LastfmGeneratorProperty.GENERATE_ALBUM_GET_INFO;
            case TRACK_GET_INFO -> LastfmGeneratorProperty.GENERATE_TRACK_GET_INFO;
            case TAG_TOP_TAGS -> LastfmGeneratorProperty.GENERATE_TAG_TOP_TAGS;
            case TAG_TOP_ARTISTS -> LastfmGeneratorProperty.GENERATE_TAG_TOP_ARTISTS;
            case TAG_TOP_TRACKS -> LastfmGeneratorProperty.GENERATE_TAG_TOP_TRACKS;
            default -> null;
        };
        return enableProp == null || configPropertyHolder.getBoolean(enableProp);
    }
}
