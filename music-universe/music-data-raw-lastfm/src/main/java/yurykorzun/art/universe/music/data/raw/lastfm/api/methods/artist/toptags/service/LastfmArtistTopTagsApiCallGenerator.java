package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.SnapshotAttributeInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LastfmArtistTopTagsApiCallGenerator extends LastfmApiCallGenerator {

    private static final List<SnapshotAttributeInfo> snapshotAttributes = List.of(
        new SnapshotAttributeInfo(LastfmAttribute.RANK, LastfmEntityType.TAG)
    );

    private final LastfmApiCallService apiCallService;
    private final LastfmEntityService entityService;
    private final LastfmDataSnapshotService dataSnapshotService;
    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.client.methods.artist.getTopTags.dueDurationDays}")
    private int dueDurationDays;

    public LastfmArtistTopTagsApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmEntityService entityService,
        LastfmDataSnapshotService dataSnapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService
    ) {
        this.apiCallService = apiCallService;
        this.entityService = entityService;
        this.dataSnapshotService = dataSnapshotService;
        this.attributeSnapshotService = attributeSnapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TAGS;
    }

    @Override
    public void createApiCalls() {

        log.info("start generating api calls for method {}", getApiCallType().getMethod());

        // generate api call creation requests
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests();
        log.info("created {} api call creation requests", apiCallCreationRequests.size());

        // create api calls
        apiCallService.createApiCalls(apiCallCreationRequests);

        // increment 'created' counter for every data snapshot,
        // because every api call references exactly one dataSnapshot (for the specific artist)
        List<Long> snapshotIds = apiCallCreationRequests.stream()
            .map(LastfmApiCallCreateRequest::getDataSnapshotId)
            .toList();
        dataSnapshotService.incCreatedCount(snapshotIds);
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {
        // find artists without unexpired api calls of type artist.getTopTags
        List<LastfmArtist> artists = entityService.findAllUnprocessed(
            LastfmEntityType.ARTIST,
            LastfmApiCallType.ARTIST_TOP_TAGS);

        // convert artists to api call creation request
        return artists.stream()
            .map(this::prepareApiCallCreationRequest)
            .toList();
    }

    private LastfmApiCallCreateRequest prepareApiCallCreationRequest(LastfmArtist artist) {
        //  create scope snapshot
        LastfmDataSnapshot dataSnapshot = dataSnapshotService.getOrCreateSnapshotFor(LastfmApiCallType.ARTIST_TOP_TAGS, artist);
        //  create attribute_snapshots
        createAttributeSnapshotsForTagWithinArtist(artist, dataSnapshot);
        //  create api call
        LastfmApiCallCreateRequest apiCallCreateRequest = createApiCallCreationRequest(artist, dataSnapshot);

        return apiCallCreateRequest;
    }

    private List<LastfmAttributeSnapshot> createAttributeSnapshotsForTagWithinArtist(
        LastfmArtist artist,
        LastfmDataSnapshot dataSnapshot
    ) {
        return snapshotAttributes.stream()
            .map(a -> attributeSnapshotService.getOrCreateForEntity(
                dataSnapshot, a.targetEntityType(), a.attribute(), artist))
            .toList();
    }

    private LastfmApiCallCreateRequest createApiCallCreationRequest(LastfmArtist artist, LastfmDataSnapshot dataSnapshot) {
        return LastfmApiCallCreateRequest.builder()
                .entityType(LastfmEntityType.ARTIST)
                .entityId(artist.getId())
                .type(getApiCallType())
                .dataSnapshotId(dataSnapshot.getId())
                .params(generateApiCallParameters(artist))
                .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
            .build();
    }

    private Map<String, String> generateApiCallParameters(LastfmArtist artist) {
        return Map.of(
            LastfmApiConstants.PARAM_NAME_ARTIST, artist.getName(),
            LastfmApiConstants.PARAM_NAME_AUTOCORRECT, "0"
        );
    }

}
