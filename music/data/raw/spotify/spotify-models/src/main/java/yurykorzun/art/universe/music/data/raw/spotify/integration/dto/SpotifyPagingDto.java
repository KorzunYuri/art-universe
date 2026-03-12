package yurykorzun.art.universe.music.data.raw.spotify.integration.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpotifyPagingDto<T>(
        List<T> items,
        Integer total,
        Integer limit,
        Integer offset,
        String next,
        String previous
) {
}
