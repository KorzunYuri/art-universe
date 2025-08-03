package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.dto.AlbumDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumGetInfoAlbumDto extends AlbumDto {

    @JsonProperty("artist")
    private String artist;
    
    @JsonProperty("listeners")
    private int listenersCount;
    
    @JsonProperty("playcount")
    private long playCount;
    
    @JsonProperty("tracks")
    private AlbumGetInfoTracksDto tracksObject;
    
    @JsonProperty("tags")
    private AlbumGetInfoTagsDto tags;
}
