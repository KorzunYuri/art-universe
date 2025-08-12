package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDtoWithMetrics;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumGetInfoAlbumDto extends AlbumDto implements AlbumDtoWithMetrics {

    @JsonProperty("artist")
    private String artist;
    
    @JsonProperty("listeners")
    private Integer listenersCount;
    
    @JsonProperty("playcount")
    private Long playCount;
    
    @JsonProperty("tracks")
    private AlbumGetInfoTracksDto tracksObject;
    
    @JsonProperty("tags")
    private AlbumGetInfoTagsDto tags;
}
