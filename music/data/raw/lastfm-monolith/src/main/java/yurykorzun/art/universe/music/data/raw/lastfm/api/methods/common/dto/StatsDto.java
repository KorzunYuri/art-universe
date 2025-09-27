package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StatsDto {

    @JsonProperty("listeners")
    private int listeners;

    @JsonProperty("playcount")
    private long playCount;

}
