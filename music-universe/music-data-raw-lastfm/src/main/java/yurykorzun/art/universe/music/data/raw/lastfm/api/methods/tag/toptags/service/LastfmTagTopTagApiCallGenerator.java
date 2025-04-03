package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;

import java.util.*;

import static yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants.PAGE_SIZE;

@Component
public class LastfmTagTopTagApiCallGenerator extends LastfmApiCallGenerator {

    private final LastfmApiCallService apiCallService;
    private final LastfmApiCallRepository apiCallRepository;
    private final LastfmDataSnapshotService snapshotService;
    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.client.methods.tag.topTags.recordsLimit}")
    private int recordsLimit;
    @Value("${lastfm.client.methods.tag.topTags.dueDurationDays}")
    private int dueDurationDays;

    public LastfmTagTopTagApiCallGenerator(
            LastfmApiCallRepository apiCallRepository,
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmAttributeSnapshotService attributeSnapshotService
    ) {
        this.apiCallRepository = apiCallRepository;
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

        //  get or create snapshot
        LastfmDataSnapshot snapshot = snapshotService.getOrCreateSnapshotFor(getApiCallType());

        //  create attribute snapshots on first launch
        if (snapshot.getCreatedCount() == 0) {
            createAttributeSnapshots(snapshot);
        }

        //  generate api calls
        List<LastfmApiCallCreateRequest> apiCallCreationRequests = generateApiCallCreationRequests(snapshot);
        apiCallService.createApiCalls(apiCallCreationRequests);
    }

    private List<LastfmApiCallCreateRequest> generateApiCallCreationRequests(LastfmDataSnapshot snapshot) {

        final int tagsCount = Integer.MAX_VALUE;
        final int pagesNumber = Math.min(tagsCount, (int) Math.ceil((float) recordsLimit / PAGE_SIZE));

        //  find all pending non expired requests to not duplicate them
        List<LastfmApiCall> pendingCalls = apiCallRepository.findAllUnexpiredByType(getApiCallType());
        Set<Integer> pendingOffsets = new HashSet<>();
        for (LastfmApiCall pendingCall : pendingCalls) {
            pendingOffsets.add(Integer.parseInt(pendingCall.getParams().getOrDefault(LastfmApiConstants.PARAM_NAME_OFFSET, "0")));
        }

        List<LastfmApiCallCreateRequest> calls = new ArrayList<>();
        for (int i = 0; i < pagesNumber; i++) {
            // skip duplicate call
            int offset = i * PAGE_SIZE;
            if (pendingOffsets.contains(offset)) {
                continue;
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
        attributeSnapshotService.getOrCreateForEntityType(parentSnapshot, LastfmEntityType.TAG, LastfmAttribute.RELATIONS_COUNT);
        attributeSnapshotService.getOrCreateForEntityType(parentSnapshot, LastfmEntityType.TAG, LastfmAttribute.REACH);
    }

}
