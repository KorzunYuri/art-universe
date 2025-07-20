package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.repository.AlbumBindingRepository;
import yurykorzun.art.universe.music.data.master.repository.AlbumRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AlbumServiceTest {

    @Mock
    private AlbumRepository albumRepository;

    @Mock
    private AlbumBindingRepository albumBindingRepository;

    @InjectMocks
    private AlbumServiceImpl albumService;

    @Mock
    private List<BoundEntityProjection> mockBindings;

    @BeforeEach
    void setUp() {
        // Mock setup is done in individual tests
    }

    @Test
    void whenFindBoundAlbums_shouldReturnCorrectBindings() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        when(albumBindingRepository.findBoundAlbumsForDataSource(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        List<BoundEntityProjection> result = albumService.findBoundAlbums(dataSource, externalIds);

        // Then
        assertEquals(mockBindings, result);
        verify(albumBindingRepository, times(1)).findBoundAlbumsForDataSource(dataSource, externalIds);
        verifyNoMoreInteractions(albumBindingRepository);
        verifyNoInteractions(albumRepository);
    }
}
