package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmTagApiCallGenerator;

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
