package yurykorzun.art.universe.music.data.approved.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.dto.TestBoundEntityProjectionImpl;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.ArtistBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.ArtistRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ArtistServiceTest {

    @InjectMocks
    private ArtistServiceImpl artistService;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private ArtistBindingRepository bindingsRepository;

    @Test
    void shouldFindBoundArtists() {
        // given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(123L, 456L);
        
        BoundEntityProjection projection = TestBoundEntityProjectionImpl.builder()
            .externalId(123L)
            .dataSource(dataSource)
            .referenceId(1L)
            .referenceName("Radiohead")
            .build();

        when(bindingsRepository.findBoundArtistsForDataSource(dataSource, externalIds))
            .thenReturn(List.of(projection));

        // when
        List<BoundEntityProjection> result = artistService.findBoundArtists(dataSource, externalIds);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getExternalId()).isEqualTo(123L);
        assertThat(result.get(0).getReferenceName()).isEqualTo("Radiohead");
    }

    @Test
    void shouldReturnEmptyListWhenNoBindingsFound() {
        // given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = List.of(123L, 456L);
        
        when(bindingsRepository.findBoundArtistsForDataSource(dataSource, externalIds))
            .thenReturn(List.of());

        // when
        List<BoundEntityProjection> result = artistService.findBoundArtists(dataSource, externalIds);

        // then
        assertThat(result).isEmpty();
    }
}
