package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.BaseLastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmAttributeSnapshotService;

import java.util.*;

import static yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants.PAGE_SIZE;

@Component
@Slf4j
public class LastfmTagTopTagsApiCallGenerator extends BaseLastfmApiCallGenerator {

    private final LastfmApiCallService apiCallService;
    private final LastfmDataSnapshotService snapshotService;
    private final LastfmAttributeSnapshotService attributeSnapshotService;
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmTagTopTagsApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        this.apiCallService = apiCallService;
        this.snapshotService = snapshotService;
        this.attributeSnapshotService = attributeSnapshotService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    @Transactional
    public void createApiCalls() {
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests();
        apiCallService.createApiCalls(apiCallCreationRequests);
        log.info("created {} API calls for method {}", apiCallCreationRequests.size(), getApiCallType().getMethod());

        snapshotService.incCreatedCount(apiCallCreationRequests.stream().map(LastfmApiCallCreateRequest::getDataSnapshotId).toList());
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {
        int recordsLimit = configPropertyHolder.getInt(LastfmGeneratorProperty.RECORDS_LIMIT_TAG_TOP_TAGS);
        int dueDurationDays = configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_TAGS);

        final int tagsCount = Integer.MAX_VALUE;
        final int pagesNumber = Math.min(tagsCount, (int) Math.ceil((float) recordsLimit / PAGE_SIZE));

        List<LastfmApiCall> pendingCalls = apiCallService.findAllUnexpiredByType(getApiCallType());
        Set<Integer> pendingOffsets = new HashSet<>();
        for (LastfmApiCall pendingCall : pendingCalls) {
            pendingOffsets.add(Integer.parseInt(pendingCall.getParams().getOrDefault(LastfmApiConstants.PARAM_NAME_OFFSET, "0")));
        }

        List<LastfmApiCallCreateRequest> calls = new ArrayList<>();
        LastfmDataSnapshot snapshot = null;
        boolean isSnapshotRetrieved = false;
        for (int i = 0; i < pagesNumber; i++) {
            int offset = i * PAGE_SIZE;
            if (pendingOffsets.contains(offset)) {
                continue;
            }

            if (!isSnapshotRetrieved) {
                snapshot = snapshotService.getOrCreateSnapshotFor(getApiCallType());

                if (snapshot.getCreatedCount() == 0) {
                    createAttributeSnapshots(snapshot);
                }
                isSnapshotRetrieved = true;
            }
            calls.add(buildApiCallCreationRequest(snapshot, offset, dueDurationDays));
        }

        if (!calls.isEmpty()) {
            snapshotService.incCreatedCountByNumber(snapshot.getId(), calls.size());
        }

        return calls;
    }

    private LastfmApiCallCreateRequest buildApiCallCreationRequest(LastfmDataSnapshot snapshot, int offset, int dueDurationDays) {
        return LastfmApiCallCreateRequest.builder()
            .type(getApiCallType())
            .entityType(LastfmEntityType.TAG)
            .dataSnapshotId(snapshot.getId())
            .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
            .params(Map.of(LastfmApiConstants.PARAM_NAME_OFFSET, String.valueOf(offset)))
            .build();
    }

    private void createAttributeSnapshots(LastfmDataSnapshot parentSnapshot) {
        attributeSnapshotService.getOrCreateForEntityType(parentSnapshot, LastfmEntityType.TAG, LastfmAttribute.RANK);
    }
}
