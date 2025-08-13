package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.deserializer.AlbumGetInfoTracksDeserializer;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AlbumGetInfoTracksDeserializer.class)
public class AlbumGetInfoTracksDto {

    @JsonProperty("track")
    private List<AlbumGetInfoTrackDto> tracks;
}
