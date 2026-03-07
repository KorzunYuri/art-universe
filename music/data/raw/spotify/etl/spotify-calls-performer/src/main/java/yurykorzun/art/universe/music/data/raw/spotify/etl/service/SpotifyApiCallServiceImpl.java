package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;

import java.time.Instant;
import java.util.List;

@Service
public class SpotifyApiCallServiceImpl implements SpotifyApiCallService {

    private final SpotifyApiCallRepository apiCallRepository;

    public SpotifyApiCallServiceImpl(SpotifyApiCallRepository apiCallRepository) {
        this.apiCallRepository = apiCallRepository;
    }

    @Override
    public List<SpotifyApiCall> findAllCreatedUnexpired() {
        return apiCallRepository.findAllCreatedUnexpired();
    }

    @Override
    @Transactional
    public void updateApiCallStatus(SpotifyApiCall call, ApiCallStatus status) {
        call.setStatus(status);
        apiCallRepository.save(call);
    }

    @Override
    @Transactional
    public void finalizeApiCall(SpotifyApiCall call, ApiCallStatus status, Integer httpStatus, String errorMessage) {
        call.setStatus(status);
        call.setHttpStatus(httpStatus);
        call.setErrorMessage(errorMessage);
        call.setExecutedDttm(Instant.now());
        apiCallRepository.save(call);
    }
}
