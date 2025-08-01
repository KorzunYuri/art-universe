package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper.createArtist;

@ExtendWith(MockitoExtension.class)
class LastfmTrackGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;

    @Mock
    private LastfmTrackService trackService;

    @InjectMocks
    private LastfmTrackGetInfoApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnTrackGetInfo() {
        // when
        LastfmApiCallType type = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.TRACK_GET_INFO, type, "API call type should be TRACK_GET_INFO");
    }

    @Test
    void getDueDurationDays_shouldReturnConfiguredValue() {
        // given
        int expectedDays = 14;
        ReflectionTestUtils.setField(generator, "dueDurationDays", expectedDays);

        // when
        int actualDays = generator.getDueDurationDays();

        // then
        assertEquals(expectedDays, actualDays, "Due duration days should match configured value");
    }

    @Test
    void selectEntitiesForApiCalls_shouldPrioritizeTracksWithMissingStats() {
        // given
        List<LastfmTrack> tracksWithMissingStats = createTestTracks(3);
        when(trackService.findTracksForGetInfo()).thenReturn(tracksWithMissingStats);

        // when
        List<LastfmTrack> result = generator.selectEntitiesForApiCalls();

        // then
        assertEquals(tracksWithMissingStats.size(), result.size(), "Should return tracks with missing stats");
        verify(trackService).findTracksForGetInfo();
    }

    @Test
    void createApiCalls_shouldGenerateApiCallsForSelectedTracks() {
        // given
        int batchSize = 10;
        ReflectionTestUtils.setField(generator, "batchSize", batchSize);

        List<LastfmTrack> tracks = createTestTracks(3);
        when(trackService.findTracksForGetInfo()).thenReturn(tracks);

        LastfmDataSnapshot mockSnapshot = new LastfmDataSnapshot(
            LastfmApiCallType.TRACK_GET_INFO, LocalDate.now());
        ReflectionTestUtils.setField(mockSnapshot, "id", 1L);
        when(snapshotService.getOrCreateSnapshotFor(eq(LastfmApiCallType.TRACK_GET_INFO), any())).thenReturn(mockSnapshot);

        // when
        generator.createApiCalls();

        // then
        ArgumentCaptor<List<LastfmApiCallCreateRequest>> requestCaptor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(requestCaptor.capture());

        List<LastfmApiCallCreateRequest> requests = requestCaptor.getValue();
        assertEquals(tracks.size(), requests.size(), "Should create one API call per track");

        // Verify each request
        for (int i = 0; i < tracks.size(); i++) {
            LastfmApiCallCreateRequest request = requests.get(i);
            LastfmTrack track = tracks.get(i);

            assertEquals(LastfmApiCallType.TRACK_GET_INFO, request.getType(), "API call type should be TRACK_GET_INFO");
            assertEquals(LastfmEntityType.TRACK, request.getEntityType(), "Entity type should be TRACK");
            assertEquals(track.getId(), request.getEntityId(), "Entity ID should match track ID");
            assertEquals(mockSnapshot.getId(), request.getDataSnapshotId(), "Data snapshot ID should match");

            // Verify parameters
            Map<String, String> params = request.getParams();
            if (track.getMbid() != null) {
                assertEquals(track.getMbid(), params.get(LastfmApiConstants.PARAM_NAME_MBID), "MBID parameter should match track MBID");
            } else {
                assertEquals(track.getName(), params.get(LastfmApiConstants.PARAM_NAME_TRACK), "Track parameter should match track name");
                assertNotNull(params.get(LastfmApiConstants.PARAM_NAME_ARTIST), "Artist parameter should be present");
            }
        }

        // Verify snapshot counter was incremented
        verify(snapshotService).incCreatedCountByNumber(eq(mockSnapshot.getId()), eq(3));
    }

    private List<LastfmTrack> createTestTracks(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> createTestTrack((long) i + 1, "Track " + (i + 1), i % 2 == 0 ? "mbid-" + i : null))
            .toList();
    }

    private LastfmTrack createTestTrack(Long id, String name, String mbid) {
        LastfmApiCall mockApiCall = LastfmApiCall.builder()
            .id(1L)
            .type(LastfmApiCallType.ARTIST_TOP_TRACKS)
            .dataSnapshotId(1L)
            .dueDttm(java.time.Instant.now())
            .params(java.util.Map.of())
            .build();

        LastfmTrack track = LastfmTrack.builder()
            .name(name)
            .mbid(mbid)
            .url("https://example.com/track/" + id)
            .approvalStatus(ApprovalStatus.PENDING)
            .apiCall(mockApiCall)
            .artist(createArtist())
            .build();

        ReflectionTestUtils.setField(track, "id", id);
        return track;
    }
}
