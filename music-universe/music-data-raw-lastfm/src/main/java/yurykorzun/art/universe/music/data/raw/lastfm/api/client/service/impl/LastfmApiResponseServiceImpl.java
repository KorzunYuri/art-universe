package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiResponseCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.LastfmApiResponseProcessorsRegistry;

import java.io.IOException;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class LastfmApiResponseServiceImpl implements LastfmApiResponseService {

    private final LastfmApiResponseRepository repository;

    public LastfmApiResponseServiceImpl(LastfmApiResponseRepository repository) {
        this.repository = repository;
    }

    @Override
    public long create(LastfmApiResponseCreateRequest dto) {
        LastfmApiResponse response = dtoToApiResponse(dto);
        response.setStatus(ApiResponseStatus.PENDING);
        response = repository.save(response);
        return response.getId();
    }

    @Override
    public void setStatus(long id, ApiResponseStatus status) throws IllegalStateException {
        LastfmApiResponse response = repository.getReferenceById(id);
        response.setStatus(status);
        repository.save(response);
    }

    @Override
    @Transactional
    public void triggerResponsesProcessing() {
        List<LastfmApiResponse> unprocessed = repository.findAllPending();
        log.info("Unprocessed API responses: {}", unprocessed.size());
        // TODO handle concurrent processing by several Processors, then process responses in parallel
        // TODO send responses to Processor not in parallel but in batches
        unprocessed.forEach(r -> {
            try {
                LastfmApiResponseProcessor<?> processor = LastfmApiResponseProcessorsRegistry.get(
                        ((LastfmApiCallType) r.getApiCallType()).getResponseDtoClass());
                processor.process(r);
                r.setStatus(ApiResponseStatus.COMPLETED);
            } catch (IOException e) {
                r.setStatus(ApiResponseStatus.PROCESSING_ERROR);
            }
            r.setUpdatedAt(Instant.now());
            repository.save(r);
        });
        log.info("Finished processing API responses");
    }

    private static LastfmApiResponse dtoToApiResponse(LastfmApiResponseCreateRequest dto) {
        return LastfmApiResponse.builder()
                .apiCallId(dto.getApiCallId())
                .apiCallType(dto.getApiCallType())
                .responseBody(dto.getResponseBody())
            .build();
    }

}
