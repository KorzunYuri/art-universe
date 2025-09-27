package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto.TrackDto;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class AlbumGetInfoTrackDto extends TrackDto {

    @JsonProperty("duration")
    private int duration;
    
    @JsonProperty("@attr")
    private AlbumGetInfoTrackAttrDto attr;
    
    @JsonProperty("artist")
    private AlbumGetInfoTrackArtistDto artist;
    
    @Override
    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }
}
