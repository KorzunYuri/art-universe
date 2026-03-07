package yurykorzun.art.universe.music.data.raw.spotify.etl.dto;

import lombok.Builder;
import lombok.Getter;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;

import java.time.Instant;

@Builder
@Getter
public class SpotifyApiCallCreateRequest {

    private SpotifyApiCallType type;
    private String spotifyId;
    private SpotifyEntityType entityType;
    private Long entityId;
    private Instant dueDttm;
}
