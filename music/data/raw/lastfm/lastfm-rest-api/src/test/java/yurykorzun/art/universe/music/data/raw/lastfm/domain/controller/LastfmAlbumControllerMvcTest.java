package yurykorzun.art.universe.music.data.raw.lastfm.domain.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.AlbumSearchParams;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.dto.LastfmAlbumResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.common.test.archetypes.BaseMvcTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LastfmAlbumController.class)
class LastfmAlbumControllerMvcTest extends BaseMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LastfmAlbumService albumService;

    private List<LastfmAlbum> mockAlbums;
    private LastfmAlbum mockAlbum;
    private LastfmArtist mockArtist;
    private Pageable defaultPageable = PageRequest.of(0, 20, Sort.by(Sort.Order.asc("name")));

    @BeforeEach
    void setUp() {
        mockArtist = EntityCreationHelper.createArtist(builder -> 
            builder.id(1L).name("Test Artist"));
            
        mockAlbum = LastfmAlbum.builder()
            .id(1L)
            .name("Test Album")
            .mbid("mbid-123")
            .url("http://test.com/album")
            .approvalStatus(ApprovalStatus.PENDING)
            .playCount(1000L)
            .listenersCount(500)
            .artist(mockArtist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        LastfmAlbum anotherMockedAlbum = LastfmAlbum.builder()
            .id(2L)
            .name("Another Album")
            .mbid("mbid-456")
            .url("http://another.com/album")
            .approvalStatus(ApprovalStatus.APPROVED)
            .playCount(2000L)
            .listenersCount(1000)
            .artist(mockArtist)
            .apiCall(EntityCreationHelper.createApiCall())
            .build();

        mockAlbums = Arrays.asList(
            mockAlbum,
            anotherMockedAlbum
        );
    }

    @Test
    void GET_albumById_shouldReturnAlbum_whenFound() throws Exception {
        // Given
        Long albumId = 1L;
        LastfmAlbumResponseDto responseDto = LastfmAlbumResponseDto.from(mockAlbum);
        
        when(albumService.findById(eq(albumId)))
            .thenReturn(responseDto);

        String expectedJson = objectMapper.writeValueAsString(responseDto);

        // When & Then
        mockMvc.perform(get("/api/v1/albums/{id}", albumId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_albums_shouldReturnAlbumsPage() throws Exception {
        // Given
        String search = "test";
        Long minPlayCount = 1000L;
        Long minListenersCount = 500L;
        Long artistId = 1L;
        
        Page<LastfmAlbum> albumsPage = new PageImpl<>(mockAlbums, defaultPageable, mockAlbums.size());
        Page<LastfmAlbumResponseDto> pageResponse = albumsPage.map(LastfmAlbumResponseDto::from);
        
        when(albumService.findAll(any(AlbumSearchParams.class), any(Pageable.class)))
            .thenReturn(pageResponse);

        String expectedJson = objectMapper.writeValueAsString(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/albums")
                .param("search", search)
                .param("minPlayCount", minPlayCount.toString())
                .param("minListenersCount", minListenersCount.toString())
                .param("artistId", artistId.toString())
                .param("tagId", "123")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }

    @Test
    void GET_albums_shouldHandleNullFilters() throws Exception {
        // Given
        Page<LastfmAlbum> albumsPage = new PageImpl<>(mockAlbums, defaultPageable, mockAlbums.size());
        Page<LastfmAlbumResponseDto> pageResponse = albumsPage.map(LastfmAlbumResponseDto::from);
        
        when(albumService.findAll(any(AlbumSearchParams.class), any(Pageable.class)))
            .thenReturn(pageResponse);

        String expectedJson = objectMapper.writeValueAsString(pageResponse);

        // When & Then
        mockMvc.perform(get("/api/v1/albums")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));
    }
}
