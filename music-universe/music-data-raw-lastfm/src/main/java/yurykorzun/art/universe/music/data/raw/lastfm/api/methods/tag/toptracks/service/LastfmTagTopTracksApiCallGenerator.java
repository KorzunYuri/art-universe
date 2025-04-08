package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class LastfmTagTopTracksApiCallGenerator extends LastfmApiCallGenerator {

    private final LastfmApiCallService apiCallService;
    private final LastfmEntityService entityService;
    private final LastfmDataSnapshotService snapshotService;
    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.client.methods.tag.topTracks.dueDurationDays}")
    private int dueDurationDays;

    @Value("${lastfm.client.methods.tag.topTracks.usageToPageRatio}")
    private int usageToPageRatio;

    public LastfmTagTopTracksApiCallGenerator(LastfmApiCallService apiCallService, LastfmEntityService entityService, LastfmDataSnapshotService snapshotService, LastfmAttributeSnapshotService attributeSnapshotService) {
        this.apiCallService = apiCallService;
        this.entityService = entityService;
        this.snapshotService = snapshotService;
        this.attributeSnapshotService = attributeSnapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests();
        apiCallService.createApiCalls(apiCallCreationRequests);

        Map<Long, List<LastfmApiCallCreateRequest>> snapshotGroups = apiCallCreationRequests.stream()
            .collect(Collectors.groupingBy(LastfmApiCallCreateRequest::getDataSnapshotId));
        snapshotGroups.forEach((k, v) -> snapshotService.incCreatedCountByNumber(k, v.size()));
    }

    public List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {
        Sort sort = Sort.by(Sort.Direction.DESC, "usageCount");
        List<LastfmTag> unprocessed = entityService.findAllUnprocessed(
            LastfmEntityType.TAG,
            LastfmApiCallType.TAG_TOP_TRACKS,
            LastfmEntityQueryConfig.builder().sort(sort).build()
        );
        return unprocessed.stream()
            .flatMap(tag -> prepareApiCallCreationRequests(tag).stream())
            .toList();
    }

    private List<LastfmApiCallCreateRequest> prepareApiCallCreationRequests(LastfmTag tag) {
        //  create snapshot
        LastfmDataSnapshot snapshot = snapshotService.getOrCreateSnapshotFor(getApiCallType(), tag);
        //  create attribute_snapshots
        createAttributeSnapshotsForTracksWithinTag(tag, snapshot);
        //  create api call
        return createApiCallCreationRequests(tag, snapshot);
    }

    private void createAttributeSnapshotsForTracksWithinTag(LastfmTag tag, LastfmDataSnapshot snapshot) {
        // attributeSnapshotService.getOrCreateForEntity(snapshot, LastfmEntityType.TRACK, LastfmAttribute.RANK, tag);
    }

    private List<LastfmApiCallCreateRequest> createApiCallCreationRequests(LastfmTag tag, LastfmDataSnapshot snapshot) {

        return IntStream.range(1, calcPagesNumber(tag) + 1)
            .mapToObj(pageNumber -> LastfmApiCallCreateRequest.builder()
                    .type(getApiCallType())
                    .entityType(LastfmEntityType.TAG)
                    .entityId(tag.getId())
                    .dataSnapshotId(snapshot.getId())
                    .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
                    .params(generateApiCallParameters(tag, pageNumber))
                .build())
            .collect(Collectors.toList());
    }

    private Map<String, String> generateApiCallParameters(LastfmTag tag, int pageNumber) {
        return Map.of(
                LastfmApiConstants.PARAM_NAME_TAG,      tag.getName()
            ,   LastfmApiConstants.PARAM_NAME_LIMIT,    String.valueOf(LastfmApiConstants.PAGE_SIZE)
            ,   LastfmApiConstants.PARAM_NAME_PAGE,     String.valueOf(pageNumber)
        );
    }

    private int calcPagesNumber(LastfmTag tag) {
        return Math.max(1, Objects.requireNonNullElse(tag.getUsageCount(), 0) / usageToPageRatio);
    }
}
