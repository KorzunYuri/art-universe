package yurykorzun.art.universe.music.data.raw.spotify.etl.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifyApiCallRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

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
    public Optional<SpotifyApiCall> findById(long id) {
        return apiCallRepository.findById(id);
    }

    @Override
    public List<SpotifyApiCall> findDueToRetry(int batchSize) {
        return apiCallRepository.findByStatusAndNotExpired(ApiCallStatus.DUE_TO_RETRY.getCode(), batchSize);
    }

    @Override
    public List<SpotifyApiCall> findCreatedNotProduced(int batchSize) {
        return apiCallRepository.findByStatusAndNotKafkaProduced(ApiCallStatus.CREATED.getCode(), batchSize);
    }

    @Override
    @Transactional
    public void updateApiCallStatus(SpotifyApiCall call, ApiCallStatus status) {
        call.setStatus(status);
        apiCallRepository.save(call);
    }

    @Override
    @Transactional
    public void markForRetry(SpotifyApiCall call) {
        call.setStatus(ApiCallStatus.DUE_TO_RETRY);
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
