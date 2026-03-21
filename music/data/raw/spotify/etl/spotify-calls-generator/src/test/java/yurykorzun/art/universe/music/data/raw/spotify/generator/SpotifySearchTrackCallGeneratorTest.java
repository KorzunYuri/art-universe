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
class SpotifySearchTrackCallGeneratorTest {

    @Mock private JdbcTemplate jdbcTemplate;
    @Mock private SpotifyApiCallService apiCallService;
    @Mock private SpotifySearchAttemptRepository searchAttemptRepository;
    @Mock private ConfigPropertyHolder configPropertyHolder;

    @InjectMocks
    private SpotifySearchTrackCallGenerator generator;

    @SuppressWarnings("unchecked")
    @Test
    void createApiCalls_shouldSkip_whenNoUnboundTracksFound() {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE)).thenReturn(100);
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH)).thenReturn(7);
        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any())).thenReturn(List.of());

        generator.createApiCalls();

        verifyNoInteractions(apiCallService, searchAttemptRepository);
    }

    @SuppressWarnings("unchecked")
    @Test
    void createApiCalls_shouldBuildSearchStringWithArtistName_whenArtistPresent() throws SQLException {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE)).thenReturn(100);
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH)).thenReturn(7);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("God's Plan");
        when(rs.getString("artist_name")).thenReturn("Drake");

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
            .thenAnswer(inv -> {
                RowMapper<Object> mapper = inv.getArgument(1);
                return List.of(mapper.mapRow(rs, 0));
            });

        SpotifyApiCall call = mock(SpotifyApiCall.class);
        when(call.getId()).thenReturn(10L);
        SpotifySearchAttempt attempt = SpotifySearchAttempt.builder()
            .entityType(SpotifyEntityType.TRACK).masterEntityId(1L)
            .searchString("God's Plan Drake").status(SearchAttemptStatus.PENDING).build();

        when(searchAttemptRepository.saveAll(anyList())).thenReturn(List.of(attempt));
        when(apiCallService.createApiCalls(anyList())).thenReturn(List.of(call));

        generator.createApiCalls();

        ArgumentCaptor<List<SpotifyApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).getType()).isEqualTo(SpotifyApiCallType.SEARCH_TRACK);
        assertThat(captor.getValue().get(0).getEntityType()).isEqualTo(SpotifyEntityType.TRACK);
        assertThat(captor.getValue().get(0).getParams().get("q")).isEqualTo("God's Plan Drake");
    }

    @SuppressWarnings("unchecked")
    @Test
    void createApiCalls_shouldBuildSearchStringWithTrackNameOnly_whenNoArtist() throws SQLException {
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.SEARCH_BATCH_SIZE)).thenReturn(100);
        when(configPropertyHolder.getInt(SpotifyGeneratorProperty.DUE_DURATION_SEARCH)).thenReturn(7);

        ResultSet rs = mock(ResultSet.class);
        when(rs.getLong("id")).thenReturn(1L);
        when(rs.getString("name")).thenReturn("Unknown Track");
        when(rs.getString("artist_name")).thenReturn(null);

        when(jdbcTemplate.query(anyString(), any(RowMapper.class), any()))
            .thenAnswer(inv -> {
                RowMapper<Object> mapper = inv.getArgument(1);
                return List.of(mapper.mapRow(rs, 0));
            });

        SpotifyApiCall call = mock(SpotifyApiCall.class);
        when(call.getId()).thenReturn(10L);
        SpotifySearchAttempt attempt = SpotifySearchAttempt.builder()
            .entityType(SpotifyEntityType.TRACK).masterEntityId(1L)
            .searchString("Unknown Track").status(SearchAttemptStatus.PENDING).build();

        when(searchAttemptRepository.saveAll(anyList())).thenReturn(List.of(attempt));
        when(apiCallService.createApiCalls(anyList())).thenReturn(List.of(call));

        generator.createApiCalls();

        ArgumentCaptor<List<SpotifyApiCallCreateRequest>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiCallService).createApiCalls(captor.capture());
        assertThat(captor.getValue().get(0).getParams().get("q")).isEqualTo("Unknown Track");
    }

    @Test
    void getApiCallType_shouldReturnSearchTrack() {
        assertThat(generator.getApiCallType()).isEqualTo(SpotifyApiCallType.SEARCH_TRACK);
    }
}
