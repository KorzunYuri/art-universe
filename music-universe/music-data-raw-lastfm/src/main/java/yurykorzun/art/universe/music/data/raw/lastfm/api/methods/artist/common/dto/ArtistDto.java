package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistDto implements EntityDto {

    private String name;

    private String mbid; // MusicBrainz ID

    private String url;

    @Override
    public String getUniqueKey() {
        return name;
    }
}
