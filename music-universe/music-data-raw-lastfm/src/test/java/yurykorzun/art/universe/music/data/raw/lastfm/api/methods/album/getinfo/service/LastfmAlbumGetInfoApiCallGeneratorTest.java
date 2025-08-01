package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.service;

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
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumGetInfoApiCallGeneratorTest {

    @Mock
    private LastfmApiCallService apiCallService;

    @Mock
    private LastfmDataSnapshotService snapshotService;
    
    @Mock
    private LastfmEntityService entityService;

    @InjectMocks
    private LastfmAlbumGetInfoApiCallGenerator generator;

    @Test
    void getApiCallType_shouldReturnAlbumGetInfo() {
        // when
        LastfmApiCallType type = generator.getApiCallType();

        // then
        assertEquals(LastfmApiCallType.ALBUM_GET_INFO, type, "API call type should be ALBUM_GET_INFO");
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
    void createApiCalls_shouldGenerateApiCallsForSelectedAlbums() {
        // given
        List<LastfmAlbum> albums = createTestAlbums(3);
        when(entityService.<LastfmAlbum>findAllUnprocessed(
            eq(LastfmEntityType.ALBUM), 
            eq(LastfmApiCallType.ALBUM_GET_INFO), 
            any()
        )).thenReturn(albums);

        LastfmDataSnapshot mockSnapshot = new LastfmDataSnapshot(
            LastfmApiCallType.ALBUM_GET_INFO, LocalDate.now());
        ReflectionTestUtils.setField(mockSnapshot, "id", 1L);
        when(snapshotService.getOrCreateSnapshotFor(eq(LastfmApiCallType.ALBUM_GET_INFO), any())).thenReturn(mockSnapshot);

        // when
        generator.createApiCalls();

        // then
        ArgumentCaptor<List<LastfmApiCallCreateRequest>> requestCaptor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(requestCaptor.capture());

        List<LastfmApiCallCreateRequest> requests = requestCaptor.getValue();
        assertEquals(albums.size(), requests.size(), "Should create one API call per album");

        // Verify each request
        for (int i = 0; i < albums.size(); i++) {
            LastfmApiCallCreateRequest request = requests.get(i);
            LastfmAlbum album = albums.get(i);

            assertEquals(LastfmApiCallType.ALBUM_GET_INFO, request.getType(), "API call type should be ALBUM_GET_INFO");
            assertEquals(LastfmEntityType.ALBUM, request.getEntityType(), "Entity type should be ALBUM");
            assertEquals(album.getId(), request.getEntityId(), "Entity ID should match album ID");
            assertEquals(mockSnapshot.getId(), request.getDataSnapshotId(), "Data snapshot ID should match");

            // Verify parameters
            Map<String, String> params = request.getParams();
            if (album.getMbid() != null) {
                assertEquals(album.getMbid(), params.get(LastfmApiConstants.PARAM_NAME_MBID), "MBID parameter should match album MBID");
            } else {
                assertEquals(album.getName(), params.get(LastfmApiConstants.PARAM_NAME_ALBUM), "Album parameter should match album name");
                if (album.getArtist() != null) {
                    assertEquals(album.getArtist().getName(), params.get(LastfmApiConstants.PARAM_NAME_ARTIST), "Artist parameter should match artist name");
                }
            }
        }

        // Verify snapshot counter was incremented
        verify(snapshotService).incCreatedCountByNumber(eq(mockSnapshot.getId()), eq(3));
    }

    private List<LastfmAlbum> createTestAlbums(int count) {
        return IntStream.range(0, count)
            .mapToObj(i -> {
                LastfmAlbum album = createTestAlbum((long) i + 1, "Album " + (i + 1), i % 2 == 0 ? "mbid-" + i : null);
                if (i % 2 != 0) {
                    LastfmArtist artist = EntityCreationHelper.createArtist(b -> b.name("Artist " + (i + 1)));
                    ReflectionTestUtils.setField(artist, "id", (long) i + 1);
                    album.setArtist(artist);
                }
                return album;
            })
            .toList();
    }

    private LastfmAlbum createTestAlbum(Long id, String name, String mbid) {
        LastfmApiCall mockApiCall = EntityCreationHelper.createApiCall(LastfmApiCallType.ARTIST_TOP_ALBUMS);
        LastfmAlbum album = LastfmAlbum.builder()
            .name(name)
            .mbid(mbid)
            .url("https://example.com/album/" + id)
            .approvalStatus(ApprovalStatus.PENDING)
            .apiCall(mockApiCall)
            .build();

        ReflectionTestUtils.setField(album, "id", id);
        return album;
    }
}
