package yurykorzun.art.universe.music.data.raw.lastfm.etl.service.impl;

import io.micrometer.core.annotation.Timed;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.data.raw.common.etl.entity.ApiResponseStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmParserProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiResponse;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.LastfmApiResponseRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiResponseService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessor;
import yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.LastfmApiResponseProcessorsRegistry;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class LastfmApiResponseServiceImpl implements LastfmApiResponseService {

    private final LastfmApiResponseRepository repository;
    private final ConfigPropertyHolder configPropertyHolder;
    private final LastfmApiResponseServiceImpl self;

    public LastfmApiResponseServiceImpl(
        LastfmApiResponseRepository repository,
        ConfigPropertyHolder configPropertyHolder,
        @Lazy LastfmApiResponseServiceImpl self
    ) {
        this.repository = repository;
        this.configPropertyHolder = configPropertyHolder;
        this.self = self;
    }

    @Timed(value = "music.data.raw.lastfm.api.response.batch",
           description = "Lastfm API response processing duration: batch")
    @Override
    public void processResponses() {
        List<LastfmApiResponse> unprocessed = repository.findAllPending();
        log.info("Unprocessed API responses left: {}", unprocessed.size());

        for (LastfmApiResponse response : unprocessed) {
            self.processResponse(response);
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void processResponse(LastfmApiResponse response) {
        try {
            self.setStatus(response, ApiResponseStatus.PROCESSING);

            LastfmApiCallType callType = response.getApiCall().getType();

            if (!isParsingEnabled(callType)) {
                log.info("Parsing is disabled for call type {}, skipping response ID {}",
                    callType, response.getId());
                self.setStatus(response, ApiResponseStatus.PROCESSING_ERROR);
                return;
            }

            LastfmApiResponseProcessor<?> processor =
                    LastfmApiResponseProcessorsRegistry.get(callType.getResponseDtoClass());

            if (processor == null) {
                log.warn("No processor found for response type: {}", callType);
                self.setStatus(response, ApiResponseStatus.PROCESSING_ERROR);
                return;
            }

            log.info("Start processing API response ID {} from method {}",
                    response.getId(), processor.getApiCallType().getMethod());

            processor.process(response);

            self.setStatus(response, ApiResponseStatus.COMPLETED);

            log.info("Successfully processed API response ID {} from method {}",
                    response.getId(), processor.getApiCallType().getMethod());

        } catch (Exception e) {
            log.error("Error processing API response ID {}: {}", response.getId(), e.getMessage(), e);
            self.setStatus(response, ApiResponseStatus.PROCESSING_ERROR);
        }
    }

    /**
     * Update response status in a separate transaction
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setStatus(LastfmApiResponse response, ApiResponseStatus status) {
        response.setStatus(status);
        response.setUpdatedAt(Instant.now());
        repository.save(response);
    }

    private boolean isParsingEnabled(LastfmApiCallType callType) {
        LastfmParserProperty property = switch (callType) {
            case ARTIST_GET_INFO -> LastfmParserProperty.PARSE_ARTIST_GET_INFO;
            case ARTIST_GET_SIMILAR -> LastfmParserProperty.PARSE_ARTIST_GET_SIMILAR;
            case ARTIST_TOP_ALBUMS -> LastfmParserProperty.PARSE_ARTIST_TOP_ALBUMS;
            case ARTIST_TOP_TRACKS -> LastfmParserProperty.PARSE_ARTIST_TOP_TRACKS;
            case ARTIST_TOP_TAGS -> LastfmParserProperty.PARSE_ARTIST_TOP_TAGS;
            case ARTIST_SEARCH -> LastfmParserProperty.PARSE_ARTIST_SEARCH;
            case ALBUM_GET_INFO -> LastfmParserProperty.PARSE_ALBUM_GET_INFO;
            case TRACK_GET_INFO -> LastfmParserProperty.PARSE_TRACK_GET_INFO;
            case TAG_TOP_TAGS -> LastfmParserProperty.PARSE_TAG_TOP_TAGS;
            case TAG_TOP_ARTISTS -> LastfmParserProperty.PARSE_TAG_TOP_ARTISTS;
            case TAG_TOP_TRACKS -> LastfmParserProperty.PARSE_TAG_TOP_TRACKS;
        };
        return configPropertyHolder.getBoolean(property);
    }
}
