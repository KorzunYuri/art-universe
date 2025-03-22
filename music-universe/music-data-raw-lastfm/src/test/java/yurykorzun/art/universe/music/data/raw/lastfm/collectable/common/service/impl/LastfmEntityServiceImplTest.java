package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCallStatus;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Duration;
import java.time.Instant;
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
    private LastfmTagRepository tagRepository;

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
        final int firstUnprocessedTagId = processedTagsNumber + 1;
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
        LastfmTag tagToBeProcessed        = tagRepository.save(generateApprovedTag());
        LastfmTag tagWithNonPendingCall   = tagRepository.save(generateApprovedTag()); // to be processed as well
        LastfmApiCall call = generateCallForTag(tagWithNonPendingCall, apiCallType, ApiCallStatus.CANCELLED);
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
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag());
        LastfmTag tagWithPendingCall    = tagRepository.save(generateApprovedTag()); // must not be processed
        LastfmApiCall call = generatePendingCallForTag(tagWithPendingCall, apiCallType);
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
        final LastfmApiCallType correctApiCallType = LastfmApiCallType.TAG_TOP_ARTISTS;
        final LastfmApiCallType wrongApiCallType = LastfmApiCallType.TAG_TOP_TAGS;
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag());
        LastfmTag tagWithNonRelatedCall = tagRepository.save(generateApprovedTag()); // to be processed as well
        LastfmApiCall call = generatePendingCallForTag(tagWithNonRelatedCall, wrongApiCallType);
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
        LastfmTag tagToBeProcessed      = tagRepository.save(generateApprovedTag());
        LastfmTag nonApprovedTag        = tagRepository.save(generateTag(ApprovalStatus.PENDING)); // must not be processed
        LastfmApiCall call = generatePendingCallForTag(nonApprovedTag, apiCallType);
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
        return IntStream.range(0, recordsNumber)
                .mapToObj(i -> generateTag(ApprovalStatus.APPROVED, String.format("Tag %2d", i)))
            .collect(Collectors.toList());
    }

    private LastfmTag generateApprovedTag() {
        return generateTag(ApprovalStatus.APPROVED);
    }

    private LastfmTag generateTag(ApprovalStatus approvalStatus) {
        return LastfmTag.builder()
                .name(UUID.randomUUID().toString())
                .approvalStatus(approvalStatus)
            .build();
    }

    private LastfmTag generateTag(ApprovalStatus approvalStatus, String name) {
        return LastfmTag.builder()
                .name(name)
                .approvalStatus(approvalStatus)
            .build();
    }

    /**
     * Generate unexpired api call for provided tags.
     * Presence of an unexpired call means the tag shouldn't be considered for a new api call creation
     */
    private List<LastfmApiCall> generatePendingCalls(List<LastfmTag> tags, LastfmApiCallType apiCallType) {
        return tags.stream()
                .map(tag -> generatePendingCallForTag(tag, apiCallType))
                .collect(Collectors.toList());
    }

    private LastfmApiCall generatePendingCallForTag(LastfmTag tag, LastfmApiCallType apiCallType) {
        return generateCallForTag(tag, apiCallType, ApiCallStatus.PENDING);
    }

    private LastfmApiCall generateCallForTag(LastfmTag tag, LastfmApiCallType apiCallType, ApiCallStatus status) {
        return LastfmApiCall.builder()
                .type(apiCallType)
                .status(status)
                .entityType((LastfmEntityType) tag.getType())
                .entityId(tag.getId())
                .dueDttm(Instant.now().plus(Duration.ofDays(1)))
            .build();
    }

}