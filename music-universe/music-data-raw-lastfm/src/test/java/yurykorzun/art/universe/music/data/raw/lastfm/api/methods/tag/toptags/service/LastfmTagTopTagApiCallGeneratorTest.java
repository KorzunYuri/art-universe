package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants.PAGE_SIZE;

@Import(LastfmTagTopTagApiCallGenerator.class)
class LastfmTagTopTagApiCallGeneratorTest extends JpaOnlyTest {

    @MockitoBean
    private LastfmApiCallService apiCallService;
    @MockitoBean
    private LastfmApiCallRepository apiCallRepository;
    @MockitoBean
    private LastfmDataSnapshotService snapshotService;
    @MockitoBean
    private LastfmAttributeSnapshotService attributeSnapshotService;
    @MockitoBean
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmTagTopTagApiCallGenerator generator;

    private static final int ALL_API_CALLS_NUMBER = 5;
    private static final int RECORDS_LIMIT = ALL_API_CALLS_NUMBER * (PAGE_SIZE - 1) + 1; // -1 validates 'non-full' pages
    private static final LastfmEntityType ENTITY_TYPE = LastfmEntityType.TAG;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(generator, "recordsLimit", RECORDS_LIMIT);
        ReflectionTestUtils.setField(generator, "dueDurationDays", 1);

        // no pending calls by default
        when(apiCallRepository.findAllUnexpiredByType(generator.getApiCallType()))
            .thenReturn(Collections.emptyList());
    }

    void testApiCallCreation(int existingApiCallsNumber) {
        // mock snapshot
        LastfmDataSnapshot existingSnapshot = new LastfmDataSnapshot(generator.getApiCallType(), LocalDate.now());
        ReflectionTestUtils.setField(existingSnapshot, "createdCount", existingApiCallsNumber);
        when(snapshotService.getOrCreateSnapshotFor(generator.getApiCallType())).thenReturn(existingSnapshot);
        // mock api calls
        List<LastfmApiCall> existingApiCalls = IntStream.range(0, existingApiCallsNumber)
            .mapToObj(i -> LastfmApiCall.builder()
                .type(generator.getApiCallType())
                .dueDttm(Instant.now().plus(Duration.ofDays(1)))
                .dataSnapshotId(1L)
                .entityType(ENTITY_TYPE)
                .params(Map.of(LastfmApiConstants.PARAM_NAME_OFFSET, String.valueOf(i * PAGE_SIZE)))
                .build())
            .collect(Collectors.toList());
        when(apiCallRepository.findAllUnexpiredByType(generator.getApiCallType())).thenReturn(existingApiCalls);

        // run
        generator.createApiCalls();

        // check that snapshot was returned
        verify(snapshotService).getOrCreateSnapshotFor(generator.getApiCallType());

        if (existingApiCallsNumber == 0) {
            verify(attributeSnapshotService).getOrCreateForEntityType(any(), eq(ENTITY_TYPE), eq(LastfmAttribute.RANK));
            verify(attributeSnapshotService).getOrCreateForEntityType(any(), eq(ENTITY_TYPE), eq(LastfmAttribute.RELATIONS_COUNT));
            verify(attributeSnapshotService).getOrCreateForEntityType(any(), eq(ENTITY_TYPE), eq(LastfmAttribute.REACH));
        } else {
            verify(attributeSnapshotService, never()).getOrCreateForEntityType(any(), any(), any());
        }

        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        List<LastfmApiCallCreateRequest> capturedCalls = captor.getValue();
        assertEquals(ALL_API_CALLS_NUMBER - existingApiCallsNumber, capturedCalls.size(),
            String.format("Generator was expected to produce %s records", ALL_API_CALLS_NUMBER - existingApiCallsNumber));
    }

    @Test
    void testGetApiCallType_returnsTagTopTags() {
        LastfmApiCallType type = generator.getApiCallType();
        assertNotNull(type, "Generator type must not be null");
        assertEquals(LastfmApiCallType.TAG_TOP_TAGS, type, "Generator type must be TAG_TOP_TAGS");
    }

    @Test
    void testCreateApiCalls_whenNoSnapshot_thenSnapshotsAndApiCallsAreCreated() {
        testApiCallCreation(0);
    }

    @Test
    void testCreateApiCalls_whenSnapshotAndApiCallsExist_thenNoRecordsAreCreated() {
        testApiCallCreation(ALL_API_CALLS_NUMBER);
    }

    @Test
    void testCreateApiCalls_whenSomeApiCallsExist_thenMissingApiCallsAreCreated() {
        testApiCallCreation(2);
    }
}