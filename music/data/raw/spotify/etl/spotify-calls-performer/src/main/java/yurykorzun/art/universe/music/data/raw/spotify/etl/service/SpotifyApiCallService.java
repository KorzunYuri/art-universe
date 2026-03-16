package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;

import java.util.List;
import java.util.Optional;

public interface SpotifyApiCallService {

    List<SpotifyApiCall> findAllCreatedUnexpired();

    Optional<SpotifyApiCall> findById(long id);

    List<SpotifyApiCall> findDueToRetry(int batchSize);

    List<SpotifyApiCall> findCreatedNotProduced(int batchSize);

    void updateApiCallStatus(SpotifyApiCall call, ApiCallStatus status);

    void finalizeApiCall(SpotifyApiCall call, ApiCallStatus status, Integer httpStatus, String errorMessage);

    void markForRetry(SpotifyApiCall call);
}
