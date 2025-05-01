package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@Import(LastfmTagTopArtistsApiCallGenerator.class)
class LastfmTagTopArtistsApiCallGeneratorTest extends JpaOnlyTest {

    @MockitoBean
    private LastfmApiCallService apiCallService;
    @MockitoBean
    private LastfmDataSnapshotService snapshotService;
    @MockitoBean
    private LastfmAttributeSnapshotService attributeSnapshotService;
    @MockitoBean
    private LastfmEntityService entityService;

    @Autowired
    private LastfmTagTopArtistsApiCallGenerator generator;


    private static final int UNPROCESSED_TAGS_COUNT = 3;
    private static final int DUE_DURATION_DAYS = 1;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(generator, "dueDurationDays", DUE_DURATION_DAYS);
    }

    private LastfmApiCall createTagSourceApiCall(boolean isExpired) {
        return LastfmApiCall.builder()
                .dataSnapshotId(1L)
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .dueDttm(Instant.now().plus(Duration.ofDays(isExpired ? -1 : 1)))
                .params(Map.of())
            .build();
    }

    @Test
    void testGetApiCallType_returnsTagTopArtists() {
        LastfmApiCallType type = generator.getApiCallType();
        assertNotNull(type, "Generator api call type must not be null");
        assertEquals(LastfmApiCallType.TAG_TOP_ARTISTS, type, "Generator api call type must be TAG_TOP_ARTISTS");
    }

    @Test
    void givenUnexpiredTags_whenCreateApiCallsCalled_createsSnapshotsAndTagTopArtistsApiCalls() {
        LastfmApiCall apiCall = createTagSourceApiCall(true);
        List<LastfmTag> unprocessedTags = IntStream.range(0, UNPROCESSED_TAGS_COUNT)
            .mapToObj(i -> {
                LastfmTag tag = LastfmTag.builder()
                        .name(String.format("tag-%d", i))
                        .apiCall(apiCall)
                        .approvalStatus(ApprovalStatus.APPROVED)
                    .build();
                ReflectionTestUtils.setField(tag, "id", i + 1);
                return tag;
            })
            .collect(Collectors.toList());
        when(entityService.<LastfmTag>findAllUnprocessed(eq(LastfmEntityType.TAG), eq(LastfmApiCallType.TAG_TOP_ARTISTS), any()))
            .thenReturn(unprocessedTags);

        // mock created data snapshot
        LastfmDataSnapshot snapshot = new LastfmDataSnapshot(LastfmApiCallType.TAG_TOP_ARTISTS, LocalDate.now());
        long mockSnapshotId = 1L;
        ReflectionTestUtils.setField(snapshot, "id", mockSnapshotId);
        when(snapshotService.getOrCreateSnapshotFor(eq(generator.getApiCallType()), any(LastfmTag.class)))
            .thenReturn(snapshot);
        // mock created attribute snapshot
        when(attributeSnapshotService.getOrCreateForEntity(eq(snapshot), eq(LastfmEntityType.ARTIST), eq(LastfmAttribute.RANK), any(LastfmTag.class)))
            .thenAnswer(i -> {
                    LastfmTag scopeEntity = i.getArgument(3);
                    return LastfmAttributeSnapshot.builder()
                        .currentSnapshotId(((LastfmDataSnapshot) i.getArgument(0)).getId())
                        .previousSnapshotId(null)
                        .scopeEntityType(scopeEntity.getType())
                        .scopeEntityId(scopeEntity.getId())
                        .attribute(i.getArgument(2))
                        .entityType(i.getArgument(1))
                        .build();
                });

        generator.createApiCalls();

        for (LastfmTag tag : unprocessedTags) {
            verify(snapshotService).getOrCreateSnapshotFor(eq(generator.getApiCallType()), eq(tag));
        }
        // for each tag, attribute_snapshot for tag RANK must have been created
        for (LastfmTag tag : unprocessedTags) {
            verify(attributeSnapshotService).getOrCreateForEntity(
                eq(snapshot), eq(LastfmEntityType.ARTIST), eq(LastfmAttribute.RANK), eq(tag));
        }

        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        List<LastfmApiCallCreateRequest> capturedCalls = captor.getValue();

        // we expect as many api calls as many tags were unprocessed
        assertEquals(UNPROCESSED_TAGS_COUNT, capturedCalls.size(),
            String.format("Expected %s new api calls, got %s", UNPROCESSED_TAGS_COUNT, capturedCalls.size()));

        // check that params contain tag name
        capturedCalls.forEach(request -> {
            Map<String, String> params = request.getParams();
            assertEquals(LastfmEntityType.TAG, request.getEntityType());
            assertTrue(request.getEntityId() > 0);
            String tagName = params.get(LastfmApiConstants.PARAM_NAME_TAG);
            assertNotNull(tagName, "'tag' must be present in api call parameters'");
            assertTrue(tagName.startsWith("tag"), "Tag name in api call parameters is incorrect");
        });

        verify(snapshotService).incCreatedCountByNumber(eq(mockSnapshotId), eq(UNPROCESSED_TAGS_COUNT));
    }
}