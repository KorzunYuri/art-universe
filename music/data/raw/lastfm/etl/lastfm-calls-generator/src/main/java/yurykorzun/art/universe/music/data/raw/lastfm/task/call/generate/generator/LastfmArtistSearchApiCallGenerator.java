package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.BaseLastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO write tests for all search request related classes
@Component
@Slf4j
public class LastfmArtistSearchApiCallGenerator extends BaseLastfmApiCallGenerator {

    private final LastfmArtistSearchRequestService searchRequestService;
    private final LastfmDataSnapshotService dataSnapshotService;
    private final LastfmApiCallService apiCallService;

    private final int batchLimit = LastfmConstants.HIBERNATE_BATCH_SIZE;

    public LastfmArtistSearchApiCallGenerator(
        LastfmArtistSearchRequestService searchRequestService,
        LastfmDataSnapshotService dataSnapshotService,
        LastfmApiCallService apiCallService
    ) {
        this.dataSnapshotService = dataSnapshotService;
        this.searchRequestService = searchRequestService;
        this.apiCallService = apiCallService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_SEARCH;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        //  find unprocessed requests
        List<LastfmArtistSearchRequest> searchRequests = searchRequestService.findUnprocessed(batchLimit);

        //  create api calls
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests(searchRequests);
        apiCallService.createApiCalls(apiCallCreationRequests);

        //  update search requests' statuses
        searchRequests.forEach(r -> r.setProcessed(true));
        searchRequestService.saveRequests(searchRequests);
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests(List<LastfmArtistSearchRequest> searchRequests) {
        LastfmDataSnapshot dataSnapshot = dataSnapshotService.getOrCreateSnapshotFor(getApiCallType());
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = searchRequests.stream()
            .map(req -> LastfmApiCallCreateRequest.builder()
                    .type(getApiCallType())
                    .dataSnapshotId(dataSnapshot.getId())
                    .entityType(LastfmEntityType.ARTIST)
                    .params(Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, req.getSearchString()))
                    .dueDttm(getDueDttm())
                .build())
            .collect(Collectors.toList());
        return apiCallCreationRequests;
    }

    private Instant getDueDttm() {
        return TimeUtil.calcDueDttm(365);
    }
}
