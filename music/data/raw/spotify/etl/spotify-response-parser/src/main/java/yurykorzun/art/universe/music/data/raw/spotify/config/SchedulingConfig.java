package yurykorzun.art.universe.music.data.raw.spotify.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.common.pgnotify.PgNotificationLoop;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.task.response.process.SpotifyApiResponseProcessingScheduler;

import javax.sql.DataSource;

@Configuration
@EnableScheduling
@Profile("!test")
public class SchedulingConfig {

    @Bean(destroyMethod = "stop")
    public PgNotificationLoop responsesNotificationLoop(
        SpotifyApiResponseProcessingScheduler scheduler,
        DataSource dataSource,
        ConfigPropertyHolder configPropertyHolder
    ) {
        PgNotificationLoop loop = new PgNotificationLoop(
            SpotifyConstants.NOTIFY_RESPONSES_READY,
            scheduler::executeWork,
            () -> configPropertyHolder.getInt(SpotifyParserProperty.SCHEDULE_DELAY_SECS) * 1000,
            dataSource
        );
        loop.start();
        return loop;
    }
}
