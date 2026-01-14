package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.beans.Transient;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistDto implements EntityDto<LastfmArtist> {

    @JsonProperty("name")
    private String name;

    @JsonProperty("mbid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mbid; // MusicBrainz ID

    @JsonProperty("url")
    private String url;

    @Override
    @Transient
    public LastfmEntityType getEntityType() {
        return LastfmEntityType.ARTIST;
    }
}
