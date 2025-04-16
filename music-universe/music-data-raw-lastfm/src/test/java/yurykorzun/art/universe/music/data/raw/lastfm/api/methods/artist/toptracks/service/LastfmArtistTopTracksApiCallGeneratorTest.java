package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.service;

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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Import(LastfmArtistTopTracksApiCallGenerator.class)
class LastfmArtistTopTracksApiCallGeneratorTest extends JpaOnlyTest {

    @MockitoBean
    private LastfmApiCallService apiCallService;
    @MockitoBean
    private LastfmDataSnapshotService dataSnapshotService;
    @MockitoBean
    private LastfmAttributeSnapshotService attributeSnapshotService;
    @MockitoBean
    private LastfmEntityService entityService;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private LastfmArtistTopTracksApiCallGenerator generator;

    private static final int UNPROCESSED_ARTISTS_COUNT = 3;
    private static final int DUE_DURATION_DAYS = 1;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(generator, "dueDurationDays", DUE_DURATION_DAYS);
    }

    private LastfmApiCall createArtistSourceApiCall(boolean isExpired) {
        return consistencyHelper.createAndSaveApiCall(builder -> builder
            .type(LastfmApiCallType.TAG_TOP_ARTISTS)
            .dueDttm(Instant.now().plus(Duration.ofDays(isExpired ? -1 : 1)))
            .params(Map.of())
        );
    }

    @Test
    void testGetApiCallType_returnsArtistTopTracks() {
        LastfmApiCallType type = generator.getApiCallType();
        assertNotNull(type, "Generator api call type must not be null");
        assertEquals(LastfmApiCallType.ARTIST_TOP_TRACKS, type);
    }

    @Test
    void givenArtist_whenCreateApiCallsCalled_createsSnapshotsAndArtistTopTracksApiCalls() {
        LastfmApiCall apiCall = createArtistSourceApiCall(true);
        List<LastfmArtist> unprocessedArtists = IntStream.range(0, UNPROCESSED_ARTISTS_COUNT)
            .mapToObj(i -> {
                LastfmArtist artist = LastfmArtist.builder()
                    .name(String.format("artist-%d", i))
                    .mbid(i % 2 == 0 ? null : String.format("mbid-%d", i))
                    .apiCall(apiCall)
                    .approvalStatus(ApprovalStatus.APPROVED)
                    .build();
                ReflectionTestUtils.setField(artist, "id", i + 1);
                return artist;
            })
            .collect(Collectors.toList());
        when(entityService.<LastfmArtist>findAllUnprocessed(eq(LastfmEntityType.ARTIST), eq(generator.getApiCallType()), any()))
            .thenReturn(unprocessedArtists);

        when(dataSnapshotService.getOrCreateSnapshotFor(eq(generator.getApiCallType()), any(LastfmArtist.class)))
            .thenAnswer(invocation -> {
                LastfmArtist artist = invocation.getArgument(1);
                LastfmDataSnapshot snapshot = new LastfmDataSnapshot(generator.getApiCallType(), LocalDate.now(), artist);
                ReflectionTestUtils.setField(snapshot, "id", artist.getId());
                return snapshot;
            });

        generator.createApiCalls();

        // check that data snapshot was requested and its created counter was incremented
        for (LastfmArtist artist : unprocessedArtists) {
            verify(dataSnapshotService).getOrCreateSnapshotFor(eq(generator.getApiCallType()), eq(artist));
        }
        ArgumentCaptor<List<Long>> snapshotCreatedCountIncCaptor = ArgumentCaptor.forClass(List.class);
        verify(dataSnapshotService).incCreatedCount(snapshotCreatedCountIncCaptor.capture());
        assertEquals(UNPROCESSED_ARTISTS_COUNT, snapshotCreatedCountIncCaptor.getValue().size());

        ArgumentCaptor<List<LastfmApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        List<LastfmApiCallCreateRequest> capturedCalls = captor.getValue();

        // we expect as many api calls as many artists were unprocessed
        assertEquals(UNPROCESSED_ARTISTS_COUNT, capturedCalls.size(),
            String.format("Expected %s new api calls, got %s", UNPROCESSED_ARTISTS_COUNT, capturedCalls.size()));

        // check that params contain tag name
        capturedCalls.forEach(request -> {
            Map<String, String> params = request.getParams();

            // check that entity type and id are present and correct
            assertEquals(LastfmEntityType.ARTIST, request.getEntityType());
            assertTrue(request.getEntityId() > 0);

            // check that call parameters contain artist name
            String artistName = params.get(LastfmApiConstants.PARAM_NAME_ARTIST);
            String artistMbid = params.get(LastfmApiConstants.PARAM_NAME_MBID);
            boolean isNamePresent = artistName != null;
            boolean isMbidPresent = artistMbid != null;
            // check that only one value is present
            assertTrue(isNamePresent != isMbidPresent);
            if (isNamePresent) {
                assertTrue(artistName.startsWith("artist"), "artist name in api call parameters is incorrect");
            } else {
                assertTrue(artistMbid.startsWith("mbid"), "artist mbid in api call parameters is incorrect");
            }

            // check that call parameters contain autocorrect disabled
            String autocorrect = params.get(LastfmApiConstants.PARAM_NAME_AUTOCORRECT);
            assertNotNull(autocorrect, "'autocorrect' parameter must be present in api call parameters'");
            assertEquals("0", autocorrect);

            // check that call parameters contain autocorrect disabled
            String pageSize = params.get(LastfmApiConstants.PARAM_NAME_LIMIT);
            assertNotNull(autocorrect, "'limit' parameter must be present in api call parameters'");
            assertEquals(String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE), pageSize);
        });
    }


}