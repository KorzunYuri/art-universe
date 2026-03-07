package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;

import java.util.List;

public interface SpotifyApiCallService {

    void createApiCalls(List<SpotifyApiCallCreateRequest> requests);
}
