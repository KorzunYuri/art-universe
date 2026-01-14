package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.util.*;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistServiceImplTest {
    
    @Mock
    private LastfmArtistRepository artistRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private LastfmArtistServiceImpl artistService;

    private LastfmArtist createArtist() {
        return EntityCreationHelper.createArtist();
    }
    private LastfmArtist createArtist(String url) {
        return EntityCreationHelper.createArtist(builder -> builder.url(url));
    }


    private LastfmArtist createArtist(Consumer<LastfmArtist.LastfmArtistBuilder<?,?>> overrideDefaults) {
        LastfmArtist.LastfmArtistBuilder<?, ?> builder = LastfmArtist.builder()
            .id(1L)
            .name("Test Artist")
            .approvalStatus(ApprovalStatus.APPROVED)
            .apiCall(mock(LastfmApiCall.class));
        overrideDefaults.accept(builder);
        return builder.build();
    }

    @Test
    void findById_shouldReturnArtistWhenExists() {
        // Given
        long artistId = 42L;
        LastfmArtist expectedArtist = createArtist(b -> b.id(artistId));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(expectedArtist));

        // When
        Optional<LastfmArtist> result = artistService.findById(artistId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(expectedArtist, result.get());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void findById_shouldReturnEmptyOptionalWhenArtistDoesNotExist() {
        // Given
        long artistId = 999L;
        when(artistRepository.findById(artistId)).thenReturn(Optional.empty());

        // When
        Optional<LastfmArtist> result = artistService.findById(artistId);

        // Then
        assertFalse(result.isPresent());
        verify(artistRepository).findById(artistId);
    }

    @Test
    void saveAll_withValidAll_shouldCallRepository() {
        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        when(artistRepository.saveAll(artists)).thenReturn(artists);

        List<LastfmArtist> savedArtists = artistService.saveAll(artists);

        assertNotNull(savedArtists);
        assertEquals(artists.size(), savedArtists.size());
        assertEquals(artists, savedArtists);
        verify(artistRepository, times(1)).saveAll(artists);
    }
}
