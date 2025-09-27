package yurykorzun.art.universe.music.data.raw.lastfm.api.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "lastfm.threshold")
@Data
public class LastfmThresholdConfig {

    private ArtistThreshold artist = new ArtistThreshold();
    private AlbumThreshold album = new AlbumThreshold();
    private TrackThreshold track = new TrackThreshold();
    private TagThreshold tag = new TagThreshold();

    @Data
    public static class ArtistThreshold {
        private Integer listenersCount = -1;
    }

    @Data
    public static class AlbumThreshold {
        private Long playCount = -1L;
    }

    @Data
    public static class TrackThreshold {
        private Long playCount = -1L;
    }

    @Data
    public static class TagThreshold {
        private Integer usageCount = -1;
    }
}
