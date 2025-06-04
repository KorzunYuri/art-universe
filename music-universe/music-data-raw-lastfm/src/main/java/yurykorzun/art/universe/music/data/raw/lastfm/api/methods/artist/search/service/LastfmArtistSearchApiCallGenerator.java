package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.service;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtistSearchRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistSearchRequestService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// TODO write tests for all search request related classes
@Component
@Slf4j
public class LastfmArtistSearchApiCallGenerator extends LastfmApiCallGenerator {

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
