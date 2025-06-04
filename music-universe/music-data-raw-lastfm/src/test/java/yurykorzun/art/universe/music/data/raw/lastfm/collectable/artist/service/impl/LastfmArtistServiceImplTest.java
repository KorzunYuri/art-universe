package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.impl;

import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository.LastfmArtistRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LastfmArtistServiceImplTest {
    
    @Mock
    private LastfmArtistRepository artistRepository;

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
    void saveArtist_withValidArtist_shouldCallRepository() {
        LastfmArtist artist = createArtist();
        when(artistRepository.save(artist)).thenReturn(artist);

        LastfmArtist savedArtist = artistService.saveArtist(artist);

        assertNotNull(savedArtist);
        assertEquals(artist, savedArtist);
        verify(artistRepository, times(1)).save(artist);
    }

    @Test
    void saveArtists_withValidArtists_shouldCallRepository() {
        List<LastfmArtist> artists = List.of(createArtist(), createArtist());
        when(artistRepository.saveAll(artists)).thenReturn(artists);

        List<LastfmArtist> savedArtists = artistService.saveArtists(artists);

        assertNotNull(savedArtists);
        assertEquals(artists.size(), savedArtists.size());
        assertEquals(artists, savedArtists);
        verify(artistRepository, times(1)).saveAll(artists);
    }

    @Test
    void findAllByUrls_withValidUrls_shouldCallRepository() {
        final int artistsNumber = 3;
        List<String> names = IntStream.range(0, artistsNumber).mapToObj(i -> UUID.randomUUID().toString()).toList();
        List<LastfmArtist> artists = names.stream()
            .map(this::createArtist)
            .toList();
        when(artistRepository.findAllByNameIn(names)).thenReturn(artists);

        List<LastfmArtist> foundArtists = artistService.findAllByNames(names);

        assertNotNull(foundArtists);
        assertEquals(artists.size(), foundArtists.size());
        assertEquals(artists, foundArtists);
        verify(artistRepository, times(1)).findAllByNameIn(names);
    }

    @Test
    void updateApprovalStatus_withValidRequest_shouldReturnUpdatedArtist() {
        long artistId = 42L;
        ApprovalStatus oldStatus = ApprovalStatus.PENDING;
        ApprovalStatus newStatus = ApprovalStatus.APPROVED;

        LastfmArtist existing = createArtist(b -> b.approvalStatus(oldStatus));
        LastfmArtist updated = createArtist(b -> b.approvalStatus(newStatus));

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(existing));
        when(artistRepository.save(any(LastfmArtist.class))).thenReturn(updated);

        LastfmArtist result = artistService.updateApprovalStatus(artistId, newStatus.getCode());

        assertEquals(newStatus, result.getApprovalStatus());
        verify(artistRepository).save(existing);
    }

    @Test
    void updateApprovalStatus_withNonexistingArtist_shouldThrowException() {
        when(artistRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
            () -> artistService.updateApprovalStatus(1L, ApprovalStatus.APPROVED.getCode())
        );
    }

    @Test
    void updateApprovalStatus_withInvalidApprovalStatusCode_shouldThrowException() {
        assertThrows(IllegalArgumentException.class,
            () -> artistService.updateApprovalStatus(1L, -1)
        );
    }
}