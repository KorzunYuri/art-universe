package yurykorzun.art.universe.music.data.approved.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.approved.dto.BoundEntityProjection;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.repository.TrackBindingRepository;
import yurykorzun.art.universe.music.data.approved.repository.TrackRepository;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackServiceTest {

    @Mock
    private TrackRepository trackRepository;

    @Mock
    private TrackBindingRepository trackBindingRepository;

    @InjectMocks
    private TrackServiceImpl trackService;

    @Mock
    private List<BoundEntityProjection> mockBindings;

    @BeforeEach
    void setUp() {
        // Mock setup is done in individual tests
    }

    @Test
    void whenFindBoundTracks_shouldReturnCorrectBindings() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        List<Long> externalIds = Arrays.asList(101L, 102L, 999L);
        
        when(trackBindingRepository.findBoundTracksForDataSource(dataSource, externalIds))
            .thenReturn(mockBindings);

        // When
        List<BoundEntityProjection> result = trackService.findBoundTracks(dataSource, externalIds);

        // Then
        assertEquals(mockBindings, result);
        verify(trackBindingRepository, times(1)).findBoundTracksForDataSource(dataSource, externalIds);
        verifyNoMoreInteractions(trackBindingRepository);
        verifyNoInteractions(trackRepository);
    }
}
