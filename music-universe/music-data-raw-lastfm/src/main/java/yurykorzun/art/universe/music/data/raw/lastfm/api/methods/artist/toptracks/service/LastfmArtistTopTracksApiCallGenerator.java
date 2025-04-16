package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LastfmArtistTopTracksApiCallGenerator extends LastfmApiCallGenerator {

    private final LastfmEntityService entityService;
    private final LastfmDataSnapshotService dataSnapshotService;
    private final LastfmApiCallService apiCallService;

    @Value("${lastfm.client.methods.artist.getTopTracks.dueDurationDays}")
    private int dueDurationDays;

    public LastfmArtistTopTracksApiCallGenerator(
        LastfmEntityService entityService, LastfmDataSnapshotService dataSnapshotService,
        LastfmApiCallService apiCallService
    ) {
        this.entityService = entityService;
        this.dataSnapshotService = dataSnapshotService;
        this.apiCallService = apiCallService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TRACKS;
    }

    @Override
    public void createApiCalls() {
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests();
        apiCallService.createApiCalls(apiCallCreationRequests);
        log.info("created {} api calls of type {}", apiCallCreationRequests.size(), getApiCallType());

        List<Long> snapshotIds = apiCallCreationRequests.stream()
            .map(LastfmApiCallCreateRequest::getDataSnapshotId)
            .toList();
        dataSnapshotService.incCreatedCount(snapshotIds);
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {
        LastfmEntityQueryConfig config = LastfmEntityQueryConfig.builder()
                .sort(Sort.by(Sort.Direction.DESC, "listenersCount"))
                .approvedEntitiesOnly(false)
            .build();

        List<LastfmArtist> artists = entityService.findAllUnprocessed(
            LastfmEntityType.ARTIST, LastfmApiCallType.ARTIST_TOP_TRACKS, config);

        return artists.stream().map(this::generateApiCallCreationRequest).toList();
    }

    private LastfmApiCallCreateRequest generateApiCallCreationRequest(LastfmArtist artist) {
        LastfmDataSnapshot dataSnapshot = dataSnapshotService.getOrCreateSnapshotFor(getApiCallType(), artist);
        return LastfmApiCallCreateRequest.builder()
                .type(getApiCallType())
                .entityType(LastfmEntityType.ARTIST)
                .entityId(artist.getId())
                .dataSnapshotId(dataSnapshot.getId())
                .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
                .params(getApiCallParams(artist))
            .build();
    }

    private Map<String, String> getApiCallParams(LastfmArtist artist) {
        Map<String, String> params = new HashMap<>();
        // limit tracks to hibernate size for now, consider configuring this property later
        params.put(LastfmApiConstants.PARAM_NAME_LIMIT, String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE));
        params.put(LastfmApiConstants.PARAM_NAME_AUTOCORRECT, "0");
        if (artist.getMbid() != null) {
            params.put(LastfmApiConstants.PARAM_NAME_MBID, artist.getMbid());
        } else {
            params.put(LastfmApiConstants.PARAM_NAME_ARTIST, artist.getName());
        }
        return params;
    }
}
