package yurykorzun.art.universe.music.data.raw.lastfm.api.client.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LastfmMigrationConfig {

    /**
     * A field for api response processing synchronization. We don't want to process API responses while migration is in progress.
     */
    @Getter
    @Setter
    private volatile boolean isApiResponseBodyMigrationInProgress = true;

}
