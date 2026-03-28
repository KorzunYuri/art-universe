package yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scheduler;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.config.LastfmTriggerProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner.LastfmAlbumScanner;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner.LastfmArtistScanner;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.trigger.scanner.LastfmTrackScanner;

import java.time.Instant;

@Component
public class AnalysisTriggerScheduler {

    private static final Logger log = LoggerFactory.getLogger(AnalysisTriggerScheduler.class);

    private final LastfmArtistScanner artistScanner;
    private final LastfmAlbumScanner albumScanner;
    private final LastfmTrackScanner trackScanner;
    private final ThreadPoolTaskScheduler taskScheduler;
    private final ConfigPropertyHolder configPropertyHolder;

    private volatile boolean running = true;

    public AnalysisTriggerScheduler(
        LastfmArtistScanner artistScanner,
        LastfmAlbumScanner albumScanner,
        LastfmTrackScanner trackScanner,
        ThreadPoolTaskScheduler taskScheduler,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.artistScanner = artistScanner;
        this.albumScanner = albumScanner;
        this.trackScanner = trackScanner;
        this.taskScheduler = taskScheduler;
        this.configPropertyHolder = configPropertyHolder;
    }

    @PostConstruct
    public void start() {
        scheduleNext();
    }

    @PreDestroy
    public void stop() {
        running = false;
    }

    private void scheduleNext() {
        if (!running) {
            return;
        }
        long delaySecs = configPropertyHolder.getInt(LastfmTriggerProperty.SCAN_INTERVAL_SECS);
        taskScheduler.schedule(this::executeAndReschedule, Instant.now().plusSeconds(delaySecs));
    }

    private void executeAndReschedule() {
        try {
            log.info("Starting LastFM analysis scan");

            int artists = artistScanner.scanAndSubmit();
            int albums = albumScanner.scanAndSubmit();
            int tracks = trackScanner.scanAndSubmit();

            log.info("LastFM analysis scan completed: {} artist, {} album, {} track tickets submitted",
                    artists, albums, tracks);
        } catch (Exception ex) {
            log.error("Error during LastFM analysis scan", ex);
        } finally {
            scheduleNext();
        }
    }
}
