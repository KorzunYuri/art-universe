package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.album.getinfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonDeserialize(using = AlbumGetInfoTagsDeserializer.class)
public class AlbumGetInfoTagsDto {

    @JsonProperty("tag")
    private List<AlbumGetInfoTagDto> tags;
}
