package yurykorzun.art.universe.music.data.raw.spotify.generator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.spotify.config.SpotifyGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.dto.SpotifyApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SearchAttemptStatus;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCall;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifyApiCallType;
import yurykorzun.art.universe.music.data.raw.spotify.etl.entity.SpotifySearchAttempt;
import yurykorzun.art.universe.music.data.raw.spotify.etl.repository.SpotifySearchAttemptRepository;
import yurykorzun.art.universe.music.data.raw.spotify.etl.service.SpotifyApiCallService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SpotifySearchArtistCallGeneratorTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private SpotifyApiCallService apiCallService;
    @Mock private SpotifySearchAttemptRepository searchAttemptRepository;
    @Mock private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SpotifySearchArtistCallGenerator generator;

    @SuppressWarnings("unchecked")
    @Test
    void createApiCalls_shouldSkip_whenNoUnboundArtistsFound() {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE)).thenReturn(100);
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH)).thenReturn(7);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any())).thenReturn(List.of());

        generator.createApiCalls();

        verifyNoInteractions(apiCallService, searchAttemptRepository);
    }

    @SuppressWarnings("unchecked")
    @Test
    void createApiCalls_shouldCreateCallsAndAttemptsForEachArtist() throws SQLException {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE)).thenReturn(100);
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH)).thenReturn(7);

        // Invoke the actual RowMapper lambda with mocked ResultSets so the private record is built correctly
        ResultSet rs1 = mock(ResultSet.class);
        when(rs1.getLong("id")).thenReturn(1L);
        when(rs1.getString("name")).thenReturn("Drake");

        ResultSet rs2 = mock(ResultSet.class);
        when(rs2.getLong("id")).thenReturn(2L);
        when(rs2.getString("name")).thenReturn("Kendrick Lamar");

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
            .thenAnswer(inv -> {
                RowMapper<Object> mapper = inv.getArgument(1);
                return List.of(mapper.mapRow(rs1, 0), mapper.mapRow(rs2, 1));
            });

        SpotifyApiCall call1 = mock(SpotifyApiCall.class);
        when(call1.getId()).thenReturn(10L);
        SpotifyApiCall call2 = mock(SpotifyApiCall.class);
        when(call2.getId()).thenReturn(11L);

        SpotifySearchAttempt attempt1 = SpotifySearchAttempt.builder()
            .entityType(SpotifyEntityType.ARTIST).masterEntityId(1L)
            .searchString("Drake").status(SearchAttemptStatus.PENDING).build();
        SpotifySearchAttempt attempt2 = SpotifySearchAttempt.builder()
            .entityType(SpotifyEntityType.ARTIST).masterEntityId(2L)
            .searchString("Kendrick Lamar").status(SearchAttemptStatus.PENDING).build();

        when(searchAttemptRepository.saveAll(anyList())).thenReturn(List.of(attempt1, attempt2));
        when(apiCallService.createApiCalls(anyList())).thenReturn(List.of(call1, call2));

        generator.createApiCalls();

        ArgumentCaptor<List<SpotifyApiCallCreateRequest>> callCaptor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(callCaptor.capture());
        assertThat(callCaptor.getValue()).hasSize(2);
        assertThat(callCaptor.getValue().get(0).getType()).isEqualTo(SpotifyApiCallType.SEARCH_ARTIST);
        assertThat(callCaptor.getValue().get(0).getEntityType()).isEqualTo(SpotifyEntityType.ARTIST);
        assertThat(callCaptor.getValue().get(0).getParams()).containsKey("q");

        // saveAll called twice: initial save + link apiCallId
        verify(searchAttemptRepository, times(2)).saveAll(anyList());
    }

    @Test
    void getApiCallType_shouldReturnSearchArtist() {
        assertThat(generator.getApiCallType()).isEqualTo(SpotifyApiCallType.SEARCH_ARTIST);
    }
}
