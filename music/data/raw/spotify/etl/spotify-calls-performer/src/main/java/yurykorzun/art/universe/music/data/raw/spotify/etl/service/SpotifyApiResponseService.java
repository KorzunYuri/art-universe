package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

public interface SpotifyApiResponseService {

    long createResponse(SpotifyApiCall call, String responseBody);
}
