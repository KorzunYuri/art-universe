package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptracks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service.LastfmTagApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeSnapshotService;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@Slf4j
public class LastfmTagTopTracksApiCallGenerator extends LastfmTagApiCallGenerator {

    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.tasks.calls-generate.due-duration-days.tag-top-tracks}")
    private int dueDurationDays;

    @Value("${lastfm.tasks.calls-generate.usage-to-page-ratio.tag-top-tracks}")
    private int usageToPageRatio;

    public LastfmTagTopTracksApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmApiCallEntityService entityService,
        LastfmDataSnapshotService dataSnapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService
    ) {
        super(apiCallService, dataSnapshotService, entityService);

        this.attributeSnapshotService = attributeSnapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmApiCallCreateRequest> generateApiCallCreationRequests(LastfmTag tag, LastfmDataSnapshot dataSnapshot) {
        //  create attribute_snapshots
        getOrCreateAttributeSnapshots(tag, dataSnapshot);
        //  create api call
        return createApiCallCreationRequests(tag, dataSnapshot);
    }

    @Override
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(LastfmTag entity, LastfmDataSnapshot dataSnapshot) {
        // attributeSnapshotService.getOrCreateForEntity(snapshot, LastfmEntityType.TRACK, LastfmAttribute.RANK, tag);
        return List.of();
    }

    private List<LastfmApiCallCreateRequest> createApiCallCreationRequests(LastfmTag tag, LastfmDataSnapshot snapshot) {
        return IntStream.range(1, calcPagesNumber(tag) + 1)
            .mapToObj(pageNumber -> LastfmApiCallCreateRequest.builder()
                .type(getApiCallType())
                .entityType(tag.getType())
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
