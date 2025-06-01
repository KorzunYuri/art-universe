package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LastfmArtistControllerTest {

    @Mock
    private LastfmArtistService artistService;

    @InjectMocks
    private LastfmArtistController controller;

    private void compareDtoAgainstEntity(LastfmArtistDto dto, LastfmArtist entity) {
        assertEquals(entity.getId(), dto.id());
        assertEquals(entity.getName(), dto.name());
        assertEquals(entity.getUrl(), dto.url());
        assertEquals(entity.getMbid(), dto.mbid());
        assertEquals(entity.getPlayCount(), dto.playCount());
        assertEquals(entity.getListenersCount(), dto.listenersCount());
    }

    @Test
    void getArtists_shouldReturnDtoPageWrappedInResponse() {
        // given
        String search = "radiohead";
        Pageable pageable = PageRequest.of(0, 2, Sort.by("name"));

        LastfmArtist artist1 = LastfmArtist.builder()
            .id(1L)
            .name("Radiohead")
            .url("https://example.com/radiohead")
            .mbid("mbid1")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(1000)
            .listenersCount(500)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        LastfmArtist artist2 = LastfmArtist.builder()
            .id(2L)
            .name("Radioriot")
            .url("https://example.com/radioriot")
            .mbid(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(null)
            .listenersCount(null)
            .apiCall(mock(LastfmApiCall.class))
            .build();

        List<LastfmArtist> artistList = List.of(artist1, artist2);
        Page<LastfmArtist> artistPage = new PageImpl<>(artistList, pageable, artistList.size());

        when(artistService.findByName(search, pageable)).thenReturn(artistPage);

        // when
        ResponseEntity<ResponseWrapper<Page<LastfmArtistDto>>> response = controller.getArtists(search, pageable);

        // then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        ResponseWrapper<Page<LastfmArtistDto>> body = response.getBody();
        assertNotNull(body);
        assertTrue(body.isSuccess());

        Page<LastfmArtistDto> data = body.getData();
        assertNotNull(data);
        assertEquals(2, data.getTotalElements());

        for (int i = 0; i < artistList.size(); i++) {
            compareDtoAgainstEntity(data.getContent().get(i), artistList.get(i));
        }
    }

    @Test
    void getArtists_shouldReturnFailureOnException() {
        // given
        Pageable pageable = PageRequest.of(0, 10);
        when(artistService.findByName(anyString(), any())).thenThrow(new RuntimeException("Fail"));

        // when
        ResponseEntity<ResponseWrapper<Page<LastfmArtistDto>>> response = controller.getArtists("abc", pageable);

        // then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        ResponseWrapper<?> body = response.getBody();
        assertNotNull(body);
        assertFalse(body.isSuccess());
        assertTrue(body.getMessage().contains("Failed to fetch artists"));
    }
}