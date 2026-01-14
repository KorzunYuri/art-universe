package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.track.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackGetInfoAlbumDto extends AlbumDto {

    @JsonProperty("artist")
    private String artistName;

    @JsonProperty("title")
    private String name;

    @JsonProperty("position")
    private Integer position;
}
