package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.AlbumDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumGetInfoAlbumDto extends AlbumDto implements AlbumDtoWithMetrics {

    @JsonProperty("artist")
    private String artistName;
    
    @JsonProperty("listeners")
    private Integer listenersCount;
    
    @JsonProperty("playcount")
    private Long playCount;
    
    @JsonProperty("tracks")
    private AlbumGetInfoTracksDto tracksObject;
    
    @JsonProperty("tags")
    private AlbumGetInfoTagsDto tags;
}
