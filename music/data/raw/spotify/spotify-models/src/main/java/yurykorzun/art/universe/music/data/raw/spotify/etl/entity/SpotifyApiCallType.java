package yurykorzun.art.universe.music.data.raw.spotify.etl.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallType;

import java.util.*;

@Getter
public enum SpotifyApiCallType implements ApiCallType {

    ARTIST_GET(1, "artist.get", "/artists/{id}",
            Set.of("spotify_id"), Collections.emptySet()),

    ARTIST_ALBUMS(2, "artist.albums", "/artists/{id}/albums",
            Set.of("spotify_id"), Set.of("offset", "limit")),

    ALBUM_GET(3, "album.get", "/albums/{id}",
            Set.of("spotify_id"), Collections.emptySet()),

    ALBUM_TRACKS(4, "album.tracks", "/albums/{id}/tracks",
            Set.of("spotify_id"), Set.of("offset", "limit")),

    TRACK_GET(5, "track.get", "/tracks/{id}",
            Set.of("spotify_id"), Collections.emptySet()),

    SEARCH_ARTIST(6, "search.artist", "/search",
            Set.of("q", "type"), Set.of("offset", "limit")),

    SEARCH_ALBUM(7, "search.album", "/search",
            Set.of("q", "type"), Set.of("offset", "limit")),

    SEARCH_TRACK(8, "search.track", "/search",
            Set.of("q", "type"), Set.of("offset", "limit"));

    static {
        CodedRegistry.register(Arrays.asList(values()), ApiCallType.class);
    }

    private final int code;
    private final String method;
    private final String path;
    private final Collection<String> mandatoryParameters;
    private final Collection<String> optionalParameters;

    SpotifyApiCallType(int code, String method, String path,
                       Collection<String> mandatoryParameters,
                       Collection<String> optionalParameters) {
        this.code = code;
        this.method = method;
        this.path = path;
        this.mandatoryParameters = Set.copyOf(mandatoryParameters);
        this.optionalParameters = Set.copyOf(optionalParameters);
    }

    @Override public Integer getCode() { return this.code; }
    @Override public String getName() { return name(); }
    @Override public String getMethod() { return this.method; }
    @Override public String getPath() { return this.path; }
    @Override public Map<String, String> getDefaultParamValues() { return Collections.emptyMap(); }
    @Override public Collection<String> getMandatoryParams() { return this.mandatoryParameters; }
    @Override public Collection<String> getOptionalParams() { return this.optionalParameters; }
}
