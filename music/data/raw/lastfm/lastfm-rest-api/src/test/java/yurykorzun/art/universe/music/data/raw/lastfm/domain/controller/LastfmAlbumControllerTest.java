package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.entity.EntityCreationHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmAlbumControllerTest {

    @Mock
    private LastfmAlbumService albumService;

    @InjectMocks
    private LastfmAlbumController controller;

    private void compareDtoAgainstEntity(LastfmAlbumResponseDto dto, LastfmAlbum entity) {
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getUrl(), dto.url());
        assertEquals(entity.getMbid(), dto.mbid());
        assertEquals(entity.getPlayCount(), dto.playCount());
        assertEquals(entity.getListenersCount(), dto.listenersCount());
        
        // Check artist reference
        if (entity.getArtist() != null) {
            assertNotNull(dto.artist());
            assertEquals(entity.getArtist().getId(), dto.artist().id());
            assertEquals(entity.getArtist().getName(), dto.artist().name());
        } else {
            assertNull(dto.artist());
        }
    }

    @Test
    void getAlbumById_shouldReturnAlbumWhenFound() {
        // given
        Long albumId = 1L;
        LastfmArtist artist = EntityCreationHelper.createArtist(b -> b.id(2L).name("Test Artist"));
        LastfmAlbumResponseDto responseDto = new LastfmAlbumResponseDto(
            albumId, "Test Album", "https://example.com/album", "mbid123", 
            ApprovalStatus.APPROVED.getCode(), 5000L, 1000, null,
            new LastfmArtistResponseDto(
                2L, "Test Artist", "https://example.com/artist", "artist-mbid", 
                ApprovalStatus.APPROVED.getCode(), 10000L, 5000));

        when(albumService.findById(albumId)).thenReturn(responseDto);

        // when
        LastfmAlbumResponseDto result = controller.getAlbumById(albumId);

        // then
        assertNotNull(result);
        assertEquals(responseDto, result);
    }

    @Test
    void getAlbums_shouldReturnDtoPage() {
        // given
        String search = "test album";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 1L;
        Set<Integer> approvalStatuses = new HashSet<>();
        approvalStatuses.add(ApprovalStatus.APPROVED.getCode());
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder.id(artistId));
        
        LastfmAlbum album1 = LastfmAlbum.builder()
            .id(1L)
            .name("Test Album 1")
            .url("https://example.com/album1")
            .mbid("mbid1")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(1000L)
            .listenersCount(500)
            .artist(artist)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        LastfmAlbum album2 = LastfmAlbum.builder()
            .id(2L)
            .name("Test Album 2")
            .url("https://example.com/album2")
            .mbid(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(null)
            .listenersCount(null)
            .artist(artist)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        List<LastfmAlbum> albumList = List.of(album1, album2);
        Page<LastfmAlbum> albumPage = new PageImpl<>(albumList, pageable, albumList.size());
        Page<LastfmAlbumResponseDto> dtoPage = albumPage.map(LastfmAlbumResponseDto::from);

        AlbumSearchParams expectedParams = new AlbumSearchParams(search, minPlayCount, minListenersCount, artistId, approvalStatuses, null);
        when(albumService.findAll(any(AlbumSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmAlbumResponseDto> result = controller.getAlbums(search, minPlayCount, minListenersCount, artistId, approvalStatuses, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(2, result.getTotalElements());

        for (int i = 0; i < albumList.size(); i++) {
            LastfmAlbumResponseDto dto = result.getContent().get(i);
            LastfmAlbum entity = albumList.get(i);
            
            assertEquals(entity.getId(), dto.id());
            assertEquals(entity.getName(), dto.name());
            assertEquals(entity.getUrl(), dto.url());
            assertEquals(entity.getMbid(), dto.mbid());
            assertEquals(entity.getApprovalStatus().getCode(), dto.approvalStatus());
            
            // Check artist reference
            assertNotNull(dto.artist());
            assertEquals(entity.getArtist().getId(), dto.artist().id());
            assertEquals(entity.getArtist().getName(), dto.artist().name());
        }
    }
    
    @Test
    void getAlbums_shouldHandleNullFilters() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        
        LastfmAlbum album = EntityCreationHelper.createAlbum();
        Page<LastfmAlbum> albumPage = new PageImpl<>(List.of(album), pageable, 1);
        Page<LastfmAlbumResponseDto> dtoPage = albumPage.map(LastfmAlbumResponseDto::from);
        
        when(albumService.findAll(any(AlbumSearchParams.class), eq(pageable)))
            .thenReturn(dtoPage);

        // when
        Page<LastfmAlbumResponseDto> result = controller.getAlbums(null, null, null, null, null, null, pageable);

        // then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }
}
