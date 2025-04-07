package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.service;

import org.springframework.beans.factory.annotation.Value;
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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;
import java.util.Map;

@Component
public class LastfmArtistGetInfoApiCallGenerator extends LastfmApiCallGenerator {

    private final LastfmApiCallService apiCallService;
    private final LastfmEntityService entityService;
    private final LastfmDataSnapshotService snapshotService;

    @Value("${lastfm.client.methods.artist.getInfo.dueDurationDays}")
    private int dueDurationDays;

    public LastfmArtistGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmEntityService entityService,
        LastfmDataSnapshotService snapshotService
    ) {
        this.apiCallService = apiCallService;
        this.entityService = entityService;
        this.snapshotService = snapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_INFO;
    }

    @Override
    public void createApiCalls() {
        LastfmDataSnapshot snapshot = snapshotService.getOrCreateSnapshotFor(getApiCallType());
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests(snapshot);
        apiCallService.createApiCalls(apiCallCreationRequests);
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests(LastfmDataSnapshot snapshot) {
        List<LastfmArtist> unprocessed = entityService.findAllUnprocessed(LastfmEntityType.ARTIST, LastfmApiCallType.ARTIST_GET_INFO);
        return unprocessed.stream()
            .map(artist -> prepareApiCallCreationRequest(artist, snapshot))
            .toList();
    }

    private LastfmApiCallCreateRequest prepareApiCallCreationRequest(LastfmArtist artist, LastfmDataSnapshot snapshot) {
        return LastfmApiCallCreateRequest.builder()
            .type(getApiCallType())
            .entityType(LastfmEntityType.ARTIST)
            .entityId(artist.getId())
            .dataSnapshotId(snapshot.getId())
            .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
            .params(generateApiCallParameters(artist))
        .build();
    }

    private Map<String, String> generateApiCallParameters(LastfmArtist artist) {
        return Map.of(LastfmApiConstants.PARAM_NAME_ARTIST, artist.getName());
    }
}
