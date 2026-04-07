package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.ArtistDtoWithMetrics;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.StatsDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.WikiDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistGetInfoArtistDto extends ArtistDto implements ArtistDtoWithMetrics {

    @JsonProperty("streamable")
    private int streamable;

    @JsonProperty("ontour")
    private int onTour;

    @JsonProperty("stats")
    private StatsDto stats;

    @JsonProperty("similar")
    private ArtistGetInfoArtistSimilarArtistsDto similarArtistsObject;

    @JsonProperty("tags")
    private ArtistGetInfoArtistTagsDto tagsObject;

    @JsonProperty("bio")
    private WikiDto bio;

    @Override
    public Integer getListenersCount() {
        return stats.getListeners();
    }
}
