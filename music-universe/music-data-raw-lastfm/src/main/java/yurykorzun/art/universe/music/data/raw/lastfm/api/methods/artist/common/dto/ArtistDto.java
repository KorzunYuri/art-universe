package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistDto {

    private String name;

    private String mbid; // MusicBrainz ID

    private String url;

}
