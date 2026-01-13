package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.etl.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;

import java.util.List;

@Service
@Slf4j
public class LastfmApiCallServiceImpl implements LastfmApiCallService {

    private final LastfmApiCallRepository apiCallRepository;

    public LastfmApiCallServiceImpl(LastfmApiCallRepository apiCallRepository) {
        this.apiCallRepository = apiCallRepository;
    }

    @Override
    @Transactional
    public List<Long> createApiCalls(List<LastfmApiCallCreateRequest> lastfmApiCallCreateRequests) {
        List<LastfmApiCall> calls = lastfmApiCallCreateRequests.stream()
                .map(this::dtoToApiCall)
                .peek(t -> t.setStatus(ApiCallStatus.PENDING))
            .toList();

        List<LastfmApiCall> apiCalls = apiCallRepository.saveAll(calls);

        return apiCalls.stream().map(LastfmApiCall::getId).toList();
    }

    @Override
    public List<LastfmApiCall> findAllUnexpiredByType(LastfmApiCallType apiCallType) {
        return apiCallRepository.findAllUnexpiredByType(apiCallType);
    }

    private LastfmApiCall dtoToApiCall(LastfmApiCallCreateRequest dto) {
        return LastfmApiCall.builder()
                .type(dto.getType())
                .dataSnapshotId(dto.getDataSnapshotId())
                .entityType(dto.getEntityType())
                .entityId(dto.getEntityId())
                .dueDttm(dto.getDueDttm())
                .params(dto.getParams())
            .build();
    }
}
