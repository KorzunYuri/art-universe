package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.StatsDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistGetInfoArtistDto extends ArtistDto {

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

}
