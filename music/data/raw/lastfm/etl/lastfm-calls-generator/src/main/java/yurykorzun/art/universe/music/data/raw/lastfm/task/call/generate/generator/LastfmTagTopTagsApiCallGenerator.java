package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.BaseLastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
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

    @Value("${lastfm.tasks.calls-generate.records-limit.tag-top-tags}")
    private int recordsLimit;
    @Value("${lastfm.tasks.calls-generate.due-duration-days.tag-top-tags}")
    private int dueDurationDays;

    public LastfmTagTopTagsApiCallGenerator(
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmAttributeSnapshotService attributeSnapshotService
    ) {
        this.apiCallService = apiCallService;
        this.snapshotService = snapshotService;
        this.attributeSnapshotService = attributeSnapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    @Transactional
    public void createApiCalls() {

        //  generate api calls
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests();
        apiCallService.createApiCalls(apiCallCreationRequests);
        log.info("created {} API calls for method {}", apiCallCreationRequests.size(), getApiCallType().getMethod());

        snapshotService.incCreatedCount(apiCallCreationRequests.stream().map(LastfmApiCallCreateRequest::getDataSnapshotId).toList());
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {

        final int tagsCount = Integer.MAX_VALUE;
        final int pagesNumber = Math.min(tagsCount, (int) Math.ceil((float) recordsLimit / PAGE_SIZE));

        //  find all pending non expired requests to not duplicate them
        List<LastfmApiCall> pendingCalls = apiCallService.findAllUnexpiredByType(getApiCallType());
        Set<Integer> pendingOffsets = new HashSet<>();
        for (LastfmApiCall pendingCall : pendingCalls) {
            pendingOffsets.add(Integer.parseInt(pendingCall.getParams().getOrDefault(LastfmApiConstants.PARAM_NAME_OFFSET, "0")));
        }

        List<LastfmApiCallCreateRequest> calls = new ArrayList<>();
        LastfmDataSnapshot snapshot = null;
        boolean isSnapshotRetrieved = false;
        for (int i = 0; i < pagesNumber; i++) {
            // skip duplicate call
            int offset = i * PAGE_SIZE;
            if (pendingOffsets.contains(offset)) {
                continue;
            }

            if (!isSnapshotRetrieved) {
                //  get or create snapshot
                snapshot = snapshotService.getOrCreateSnapshotFor(getApiCallType());

                //  create attribute snapshots on first launch
                if (snapshot.getCreatedCount() == 0) {
                    createAttributeSnapshots(snapshot);
                }
                isSnapshotRetrieved = true;
            }
            calls.add(buildApiCallCreationRequest(snapshot, offset));
        }

        if (!calls.isEmpty()) {
            snapshotService.incCreatedCountByNumber(snapshot.getId(), calls.size());
        }

        return calls;
    }

    private LastfmApiCallCreateRequest buildApiCallCreationRequest(LastfmDataSnapshot snapshot, int offset) {
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
