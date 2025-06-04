package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrackDto implements EntityDto {

    private String name;

    private String mbid;

    private String url;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getUniqueKey() {
        return url;
    }
}
