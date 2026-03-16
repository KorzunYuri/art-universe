package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

import java.util.List;

public interface SpotifyApiCallService {

    List<SpotifyApiCall> createApiCalls(List<SpotifyApiCallCreateRequest> requests);

    void markAsProduced(long callId, String kafkaTopic);
}
