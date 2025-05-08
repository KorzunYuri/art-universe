package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistSearchArtistDto extends ArtistDto {

    @JsonProperty("listeners")
    private Integer listenersCount;

    @JsonProperty("streamable")
    private Integer streamable;

}
