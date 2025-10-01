package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.impl;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.impl.LastfmApiCallEntityServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants.HIBERNATE_BATCH_SIZE;

@Tag("integration")
@Import(LastfmApiCallEntityServiceImpl.class)
class LastfmEntityServiceImplTest extends JpaTestWithHelper {

    @Autowired
    private LastfmApiCallEntityServiceImpl service;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Test
    void unprocessedRequested_shouldLimitResultToBatchSize_whenManyEntitiesAndNoApiCallsProvided() {
        // given
        final int tagsNumber = HIBERNATE_BATCH_SIZE * 2;
        final LastfmApiCallType whateverCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        List<LastfmTag> approvedTags = generateApprovedTags(tagsNumber);
        tagRepository.saveAll(approvedTags);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, whateverCallType);

        // then
        assertEquals(HIBERNATE_BATCH_SIZE, unprocessed.size());
    }

    @Test
    void unprocessedRequested_shouldLimitResultToBatchSize_whenManyEntitiesAndSomeApiCallsProvided() {
        // given
        final int approvedTagsNumber = HIBERNATE_BATCH_SIZE * 2;
        final int processedTagsNumber = HIBERNATE_BATCH_SIZE + 1;
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        // generate tags
        List<LastfmTag> approvedTags = generateApprovedTags(approvedTagsNumber);
        List<LastfmTag> savedTags = tagRepository.saveAll(approvedTags);
        // generate pending api calls for number of tags missing calls to be less than batch size
        List<LastfmApiCall> pendingApiCalls = generatePendingCalls(savedTags.subList(0, processedTagsNumber), apiCallType);
        apiCallRepository.saveAll(pendingApiCalls);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, apiCallType);

        // then
        assertEquals(approvedTagsNumber - processedTagsNumber, unprocessed.size());
    }

    @Test
    void unprocessedRequested_shouldIgnoreEntity_whenEntityWithExistingCallProvided() {
        // given
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        LastfmApiCall tagsApiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.TAG_TOP_TAGS);
        LastfmTag tagToBeProcessed        = tagRepository.save(generateApprovedTag(tagsApiCall));
        LastfmTag tagWithExistingCall   = tagRepository.save(generateApprovedTag(tagsApiCall)); // to be processed as well
        LastfmApiCall call = generateCallForTag(tagWithExistingCall, apiCallType, ApiCallStatus.CANCELLED, tagsApiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, apiCallType);

        // then
        assertEquals(1, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
    }

    @Test
    void unprocessedRequested_shouldIgnoreEntity_whenApprovedEntityWithPendingCallProvided() {
        // given
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        LastfmApiCall tagsApiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.TAG_TOP_TAGS);
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag(tagsApiCall));
        LastfmTag tagWithPendingCall    = tagRepository.save(generateApprovedTag(tagsApiCall)); // must not be processed
        LastfmApiCall call = generatePendingCallForTag(tagWithPendingCall, apiCallType, tagsApiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, apiCallType);

        // then
        assertEquals(1, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
    }

    @Test
    void unprocessedRequested_shouldReturnEntity_whenApprovedEntityWithPendingCallOfWrongTypeProvided() {
        // given
        LastfmApiCall tagsApiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.TAG_TOP_TAGS);
        final LastfmApiCallType correctApiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        final LastfmApiCallType wrongApiCallType = LastfmApiCallType.TAG_TOP_TAGS;
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag(tagsApiCall));
        LastfmTag tagWithNonRelatedCall = tagRepository.save(generateApprovedTag(tagsApiCall)); // to be processed as well
        LastfmApiCall call = generatePendingCallForTag(tagWithNonRelatedCall, wrongApiCallType, tagsApiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, correctApiCallType);

        // then
        assertEquals(2, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
        assertTrue(unprocessed.contains(tagWithNonRelatedCall));
    }

    @Test
    void unprocessedRequested_shouldReturnEntity_whenNonApprovedEntityWithPendingCallProvided() {
        // given
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        LastfmApiCall tagsApiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.TAG_TOP_TAGS);
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag(tagsApiCall));
        LastfmTag nonApprovedTag        = tagRepository.save(generateTag(ApprovalStatus.PENDING, tagsApiCall)); // must not be processed
        LastfmApiCall call = generatePendingCallForTag(nonApprovedTag, apiCallType, tagsApiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, apiCallType);

        // then
        assertEquals(1, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
    }


    /**
     * Generate a list of APPROVED tags
     */
    private List<LastfmTag> generateApprovedTags(int recordsNumber) {
        LastfmApiCall tagsApiCall = consistencyHelper.createAndSaveApiCall(LastfmApiCallType.TAG_TOP_TAGS);
        return IntStream.range(0, recordsNumber)
                .mapToObj(i -> generateTag(ApprovalStatus.APPROVED, String.format("Tag %2d", i), tagsApiCall))
            .collect(Collectors.toList());
    }

    private LastfmTag generateApprovedTag(LastfmApiCall apiCall) {
        return generateTag(ApprovalStatus.APPROVED, apiCall);
    }

    private LastfmTag generateTag(ApprovalStatus approvalStatus, LastfmApiCall apiCall) {
        return generateTag(approvalStatus, UUID.randomUUID().toString(), apiCall);
    }

    private LastfmTag generateTag(ApprovalStatus approvalStatus, String name, LastfmApiCall apiCall) {
        return LastfmTag.builder()
                .name(name)
                .approvalStatus(approvalStatus)
                .apiCall(apiCall)
            .build();
    }

    /**
     * Generate unexpired api call for provided tags.
     * Presence of an unexpired call means the tag shouldn't be considered for a new api call creation
     */
    private List<LastfmApiCall> generatePendingCalls(List<LastfmTag> tags, LastfmApiCallType apiCallType) {
        LastfmDataSnapshot snapshot = consistencyHelper.createAndSaveDataSnapshot(apiCallType);
        return tags.stream()
                .map(tag -> generatePendingCallForTag(tag, apiCallType, snapshot.getId()))
                .collect(Collectors.toList());
    }

    private LastfmApiCall generatePendingCallForTag(LastfmTag tag, LastfmApiCallType apiCallType, long snapshotId) {
        return generateCallForTag(tag, apiCallType, ApiCallStatus.PENDING, snapshotId);
    }

    private LastfmApiCall generateCallForTag(LastfmTag tag, LastfmApiCallType apiCallType, ApiCallStatus status, long snapshotId) {
        return LastfmApiCall.builder()
                .type(apiCallType)
                .status(status)
                .entityType((LastfmEntityType) tag.getType())
                .entityId(tag.getId())
                .dueDttm(Instant.now().plus(Duration.ofDays(1)))
                .dataSnapshotId(snapshotId)
            .build();
    }

}