package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindRequestDTO;
import yurykorzun.art.universe.music.data.master.dto.binding.BatchUnbindResponseDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
class BindingServiceImplTest {

    @Mock
    private EntityManager entityManager;

    @Mock
    private Query query;

    private BindingServiceImpl bindingService;

    @BeforeEach
    void setUp() {
        bindingService = new BindingServiceImpl(entityManager);
    }

    @Test
    void batchUnbind_shouldReturnSuccessfulResponse_whenBindingsExist() {
        // Given
        List<Long> externalIds = Arrays.asList(1001L, 1002L, 1003L);
        List<Long> existingIds = Arrays.asList(1001L, 1002L);
        
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Mock finding existing IDs
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(1001L, 1002L));
        when(query.executeUpdate()).thenReturn(2);

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(3);
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getNotFoundCount()).isEqualTo(1);
        assertThat(response.getSuccessfullyUnbound()).containsExactlyInAnyOrder(1001L, 1002L);
        assertThat(response.getNotFound()).containsExactly(1003L);

        // Verify interactions
        verify(entityManager, times(2)).createNativeQuery(anyString());
        verify(query, times(7)).setParameter(anyInt(), any()); // 4 for find (1 + 3) + 3 for delete (1 + 2)
        verify(query, times(1)).getResultList();
        verify(query, times(1)).executeUpdate();
    }

    @Test
    void batchUnbind_shouldReturnAllNotFound_whenNoBindingsExist() {
        // Given
        List<Long> externalIds = Arrays.asList(9999L, 9998L);
        
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Mock finding no existing IDs
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Collections.emptyList());

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.CATEGORY, DataSource.SPOTIFY, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(2);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getNotFoundCount()).isEqualTo(2);
        assertThat(response.getSuccessfullyUnbound()).isEmpty();
        assertThat(response.getNotFound()).containsExactlyInAnyOrder(9999L, 9998L);

        // Verify interactions - only find query should be called, not delete
        verify(entityManager, times(1)).createNativeQuery(anyString());
        verify(query, times(3)).setParameter(anyInt(), any()); // Only for find query
        verify(query, times(1)).getResultList();
        verify(query, never()).executeUpdate(); // Delete should not be called
    }

    @Test
    void batchUnbind_shouldReturnEmptyResult_whenEmptyRequest() {
        // Given
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(Collections.emptyList())
            .build();

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.TRACK, DataSource.MUSICBRAINZ, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(0);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getNotFoundCount()).isEqualTo(0);
        assertThat(response.getSuccessfullyUnbound()).isEmpty();
        assertThat(response.getNotFound()).isEmpty();

        // Verify no database interactions
        verifyNoInteractions(entityManager);
    }

    @Test
    void batchUnbind_shouldReturnEmptyResult_whenNullRequest() {
        // Given
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(null)
            .build();

        // When
        BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request);

        // Then
        assertThat(response.getTotalProcessed()).isEqualTo(0);
        assertThat(response.getSuccessCount()).isEqualTo(0);
        assertThat(response.getNotFoundCount()).isEqualTo(0);
        assertThat(response.getSuccessfullyUnbound()).isEmpty();
        assertThat(response.getNotFound()).isEmpty();

        // Verify no database interactions
        verifyNoInteractions(entityManager);
    }

    @Test
    void batchUnbind_shouldThrowException_whenFindQueryFails() {
        // Given
        List<Long> externalIds = Arrays.asList(1001L);
        
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Mock database exception
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenThrow(new RuntimeException("Database error"));

        // When & Then
        assertThatThrownBy(() -> bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to find existing external IDs");
    }

    @Test
    void batchUnbind_shouldThrowException_whenDeleteQueryFails() {
        // Given
        List<Long> externalIds = Arrays.asList(1001L);
        
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Mock successful find but failed delete
        when(entityManager.createNativeQuery(anyString())).thenReturn(query);
        when(query.setParameter(anyInt(), any())).thenReturn(query);
        when(query.getResultList()).thenReturn(Arrays.asList(1001L));
        when(query.executeUpdate()).thenThrow(new RuntimeException("Delete failed"));

        // When & Then
        assertThatThrownBy(() -> bindingService.batchUnbind(MasterEntityType.ARTIST, DataSource.LASTFM, request))
            .isInstanceOf(RuntimeException.class)
            .hasMessage("Failed to delete bindings");
    }

    @Test
    void batchUnbind_shouldHandleAllEntityTypes_whenValidRequests() {
        // Given
        List<Long> externalIds = Arrays.asList(1001L);
        
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Test each entity type separately to avoid mock interaction conflicts
        for (MasterEntityType entityType : MasterEntityType.values()) {
            // Reset mocks for each iteration
            reset(entityManager, query);
            
            // Mock successful operations
            when(entityManager.createNativeQuery(anyString())).thenReturn(query);
            when(query.setParameter(anyInt(), any())).thenReturn(query);
            when(query.getResultList()).thenReturn(Arrays.asList(1001L));
            when(query.executeUpdate()).thenReturn(1);

            // When
            BatchUnbindResponseDTO response = bindingService.batchUnbind(entityType, DataSource.LASTFM, request);

            // Then
            assertThat(response.getSuccessCount()).isEqualTo(1);
            assertThat(response.getSuccessfullyUnbound()).containsExactly(1001L);
        }
    }

    @Test
    void batchUnbind_shouldHandleAllDataSources_whenValidRequests() {
        // Given
        List<Long> externalIds = Arrays.asList(1001L);
        
        BatchUnbindRequestDTO request = BatchUnbindRequestDTO.builder()
            .externalIds(externalIds)
            .build();

        // Test each data source separately to avoid mock interaction conflicts
        for (DataSource dataSource : DataSource.values()) {
            // Reset mocks for each iteration
            reset(entityManager, query);
            
            // Mock successful operations
            when(entityManager.createNativeQuery(anyString())).thenReturn(query);
            when(query.setParameter(anyInt(), any())).thenReturn(query);
            when(query.getResultList()).thenReturn(Arrays.asList(1001L));
            when(query.executeUpdate()).thenReturn(1);

            // When
            BatchUnbindResponseDTO response = bindingService.batchUnbind(MasterEntityType.ARTIST, dataSource, request);

            // Then
            assertThat(response.getSuccessCount()).isEqualTo(1);
            assertThat(response.getSuccessfullyUnbound()).containsExactly(1001L);
        }
    }
}
