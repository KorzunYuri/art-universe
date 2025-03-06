package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service.impl;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.apiclient.messaging.ApiCallMessageProducer;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.dto.LastfmApiCallRunRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.service.LastfmApiCallService;

@Service
public class LastfmApiCallServiceImpl implements LastfmApiCallService {

    private final LastfmApiCallRepository repository;
    private final ApiCallMessageProducer messageProducer;

    public LastfmApiCallServiceImpl(
            LastfmApiCallRepository repository,
            ApiCallMessageProducer messageProducer
    ) {
        this.repository = repository;
        this.messageProducer = messageProducer;
    }

    @Override
    @Transactional
    public long createRequest(LastfmApiCallCreateRequest dto) {
        LastfmApiCall lastfmApiCall = repository.save(dtoToApiCall(dto));

        messageProducer.send(getRunRequest(dto));

        return lastfmApiCall.getId();
    }

    @Override
    public void setStatus(long id, ApiCallStatus status) throws IllegalStateException {
        LastfmApiCall call = repository.getReferenceById(id);
        call.setStatus(status);
        repository.save(call);
    }

    private static LastfmApiCall dtoToApiCall(LastfmApiCallCreateRequest dto) {
        return LastfmApiCall.builder()
                .type(dto.getType())
                .dueDttm(dto.getDueDttm())
                .build();
    }

    private static LastfmApiCallRunRequest getRunRequest(LastfmApiCallCreateRequest dto) {
        return LastfmApiCallRunRequest.builder()
                .type(dto.getType())
                .dueDttm(dto.getDueDttm())
                .build();
    }
}
