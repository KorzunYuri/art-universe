package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
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
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmTagTopTracksApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmApiCallEntityService entityService,
        LastfmDataSnapshotService dataSnapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(apiCallService, dataSnapshotService, entityService);
        this.attributeSnapshotService = attributeSnapshotService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TRACKS;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TRACKS);
    }

    @Override
    protected List<LastfmApiCallCreateRequest> generateApiCallCreationRequests(LastfmTag tag, LastfmDataSnapshot dataSnapshot) {
        getOrCreateAttributeSnapshots(tag, dataSnapshot);
        return createApiCallCreationRequests(tag, dataSnapshot);
    }

    @Override
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(LastfmTag entity, LastfmDataSnapshot dataSnapshot) {
        return List.of();
    }

    private List<LastfmApiCallCreateRequest> createApiCallCreationRequests(LastfmTag tag, LastfmDataSnapshot snapshot) {
        int dueDurationDays = configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TRACKS);
        int usageToPageRatio = configPropertyHolder.getInt(LastfmGeneratorProperty.USAGE_TO_PAGE_RATIO_TAG_TOP_TRACKS);
        return IntStream.range(1, calcPagesNumber(tag, usageToPageRatio) + 1)
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
            LastfmApiConstants.PARAM_NAME_TAG,   tag.getName(),
            LastfmApiConstants.PARAM_NAME_LIMIT, String.valueOf(LastfmApiConstants.PAGE_SIZE),
            LastfmApiConstants.PARAM_NAME_PAGE,  String.valueOf(pageNumber)
        );
    }

    private int calcPagesNumber(LastfmTag tag, int usageToPageRatio) {
        return Math.max(1, Objects.requireNonNullElse(tag.getUsageCount(), 0) / usageToPageRatio);
    }
}
