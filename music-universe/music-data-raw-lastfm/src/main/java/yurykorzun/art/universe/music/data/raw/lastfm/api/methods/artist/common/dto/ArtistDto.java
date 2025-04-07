package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ArtistDto implements EntityDto {

    @JsonProperty("name")
    private String name;

    @JsonProperty("mbid")
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String mbid; // MusicBrainz ID

    @JsonProperty("url")
    private String url;

    @Override
    public String getUniqueKey() {
        return name;
    }
}
