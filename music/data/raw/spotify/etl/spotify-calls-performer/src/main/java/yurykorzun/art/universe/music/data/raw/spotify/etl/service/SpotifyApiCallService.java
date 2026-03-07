package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

import java.util.List;

public interface SpotifyApiCallService {

    List<SpotifyApiCall> findAllCreatedUnexpired();

    void updateApiCallStatus(SpotifyApiCall call, ApiCallStatus status);

    void finalizeApiCall(SpotifyApiCall call, ApiCallStatus status, Integer httpStatus, String errorMessage);
}
