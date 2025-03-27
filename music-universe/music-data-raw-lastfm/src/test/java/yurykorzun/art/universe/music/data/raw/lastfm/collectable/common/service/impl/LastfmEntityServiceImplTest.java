package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmDataSnapshotRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants.HIBERNATE_BATCH_SIZE;

@Import(LastfmEntityServiceImpl.class)
class LastfmEntityServiceImplTest extends JpaOnlyTest {

    @Autowired
    private LastfmEntityServiceImpl service;

    @Autowired
    private LastfmApiCallRepository apiCallRepository;

    @Autowired
    private LastfmDataSnapshotRepository snapshotRepository;

    @Autowired
    private LastfmTagRepository tagRepository;

    private LastfmDataSnapshot createDummyDataSnapshot(LastfmApiCallType type) {
        return snapshotRepository.save(new LastfmDataSnapshot(type, new Date()));
    }

    private LastfmApiCall createDummyTagSourceApiCall() {
        LastfmDataSnapshot snapshot = createDummyDataSnapshot(LastfmApiCallType.TAG_TOP_TAGS);
        LastfmApiCall dummyApiCall = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dataSnapshotId(snapshot.getId())
                .dueDttm(Instant.now())
                .build();
        dummyApiCall = apiCallRepository.save(dummyApiCall);
        return dummyApiCall;
    }

    @Test
    void givenManyEntitiesAndNoApiCalls_whenUnprocessedRequested_limitsResultToBatchSize() {
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
    void givenManyEntitiesAndSomeApiCalls_whenUnprocessedRequested_limitsResultToBatchSize() {
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
    void givenEntityWithNonPendingCall_whenUnprocessedRequested_returnsEntity() {
        // given
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        LastfmApiCall apiCall = createDummyTagSourceApiCall();
        LastfmTag tagToBeProcessed        = tagRepository.save(generateApprovedTag(apiCall));
        LastfmTag tagWithNonPendingCall   = tagRepository.save(generateApprovedTag(apiCall)); // to be processed as well
        LastfmApiCall call = generateCallForTag(tagWithNonPendingCall, apiCallType, ApiCallStatus.CANCELLED, apiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, apiCallType);

        // then
        assertEquals(2, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
        assertTrue(unprocessed.contains(tagWithNonPendingCall));
    }

    @Test
    void givenApprovedEntityWithPendingCall_whenUnprocessedRequested_ignoresEntity() {
        // given
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        LastfmApiCall apiCall = createDummyTagSourceApiCall();
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag(apiCall));
        LastfmTag tagWithPendingCall    = tagRepository.save(generateApprovedTag(apiCall)); // must not be processed
        LastfmApiCall call = generatePendingCallForTag(tagWithPendingCall, apiCallType, apiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, apiCallType);

        // then
        assertEquals(1, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
    }

    @Test
    void givenApprovedEntityWithPendingCallOfWrongType_whenUnprocessedRequested_returnsEntity() {
        // given
        LastfmApiCall apiCall = createDummyTagSourceApiCall();
        final LastfmApiCallType correctApiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        final LastfmApiCallType wrongApiCallType = LastfmApiCallType.TAG_TOP_TAGS;
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag(apiCall));
        LastfmTag tagWithNonRelatedCall = tagRepository.save(generateApprovedTag(apiCall)); // to be processed as well
        LastfmApiCall call = generatePendingCallForTag(tagWithNonRelatedCall, wrongApiCallType, apiCall.getDataSnapshotId());
        apiCallRepository.save(call);

        // when
        List<LastfmTag> unprocessed = service.findAllUnprocessed(LastfmEntityType.TAG, correctApiCallType);

        // then
        assertEquals(2, unprocessed.size());
        assertTrue(unprocessed.contains(tagToBeProcessed));
        assertTrue(unprocessed.contains(tagWithNonRelatedCall));
    }

    @Test
    void givenNonApprovedEntityWithPendingCall_whenUnprocessedRequested_returnsEntity() {
        // given
        final LastfmApiCallType apiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        LastfmApiCall apiCall = createDummyTagSourceApiCall();
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag(apiCall));
        LastfmTag nonApprovedTag        = tagRepository.save(generateTag(ApprovalStatus.PENDING, apiCall)); // must not be processed
        LastfmApiCall call = generatePendingCallForTag(nonApprovedTag, apiCallType, apiCall.getDataSnapshotId());
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
        LastfmApiCall apiCall = createDummyTagSourceApiCall();
        return IntStream.range(0, recordsNumber)
                .mapToObj(i -> generateTag(ApprovalStatus.APPROVED, String.format("Tag %2d", i), apiCall))
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
        LastfmDataSnapshot snapshot = createDummyDataSnapshot(apiCallType);
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