package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.EntityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.exception.DataAccessException;
import yurykorzun.art.universe.music.data.master.exception.EntityBindingException;
import yurykorzun.art.universe.music.data.master.service.RelationService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RelationControllerTest {

    @Mock
    private RelationService relationService;

    @InjectMocks
    private RelationController relationController;

    @Test
    void bindExternalRelation_shouldReturnBindingDTO() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;

        RelationBindingDTO expectedBinding = RelationBindingDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .targetExternalId(targetExternalEntityId)
            .dataSource(dataSource)
            .relationId(789L)
            .sourceEntityName("Artist Name")
            .targetEntityName("Category Name")
            .sourceEntityType(sourceEntityType)
            .targetEntityType(targetEntityType)
            .build();

        when(relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenReturn(expectedBinding);

        // When
        RelationBindingDTO result = relationController.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );

        // Then
        assertEquals(expectedBinding, result);
        verify(relationService).bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void bindExternalRelation_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            relationController.bindExternalRelation(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
            )
        );
        
        assertEquals("Failed to bind relation: " + errorMessage, exception.getMessage());
        verify(relationService).bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void unbindExternalRelation_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;

        when(relationService.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenReturn(true);

        // When
        boolean result = relationController.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );

        // Then
        assertTrue(result);
        verify(relationService).unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void unbindExternalRelation_whenExceptionThrown_shouldThrowEntityBindingException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        EntityBindingException exception = assertThrows(EntityBindingException.class, () -> 
            relationController.unbindExternalRelation(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
            )
        );
        
        assertEquals("Failed to unbind relation: " + errorMessage, exception.getMessage());
        verify(relationService).unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void findBoundExternalRelations_shouldReturnBindingStatusDTO() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(456L, 789L);

        RelationBindingStatusDTO expectedStatus = RelationBindingStatusDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .sourceEntityType(sourceEntityType)
            .sourceEntityName("Artist Name")
            .sourceInternalId(1L)
            .sourceEntityBound(true)
            .targetEntityType(targetEntityType)
            .targetBindings(Arrays.asList(
                TargetEntityBindingDTO.builder()
                    .targetExternalId(456L)
                    .targetEntityName("Category 1")
                    .targetInternalId(2L)
                    .targetEntityBound(true)
                    .internalRelationBound(true)
                    .externalRelationBound(true)
                    .internalRelationId(10L)
                    .build(),
                TargetEntityBindingDTO.builder()
                    .targetExternalId(789L)
                    .targetEntityName("Category 2")
                    .targetInternalId(3L)
                    .targetEntityBound(true)
                    .internalRelationBound(false)
                    .externalRelationBound(false)
                    .internalRelationId(null)
                    .build()
            ))
            .build();

        when(relationService.findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityIds)
        )).thenReturn(expectedStatus);

        // When
        RelationBindingStatusDTO result = relationController.findBoundExternalRelations(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds
        );

        // Then
        assertEquals(expectedStatus, result);
        verify(relationService).findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityIds)
        );
    }

    @Test
    void findBoundExternalRelations_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(456L, 789L);
        String errorMessage = "Test error";

        when(relationService.findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityIds)
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            relationController.findBoundExternalRelations(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds
            )
        );
        
        assertEquals("Failed to get bound relations: " + errorMessage, exception.getMessage());
        verify(relationService).findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityIds)
        );
    }

    @Test
    void getRelatedEntities_shouldReturnEntityDTOList() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;

        List<EntityDTO> expectedEntities = Arrays.asList(
            EntityDTO.builder()
                .id(1L)
                .name("Category 1")
                .entityType(targetEntityType)
                .build(),
            EntityDTO.builder()
                .id(2L)
                .name("Category 2")
                .entityType(targetEntityType)
                .build()
        );

        when(relationService.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        )).thenReturn(expectedEntities);

        // When
        List<EntityDTO> result = relationController.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );

        // Then
        assertEquals(expectedEntities, result);
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );
    }

    @Test
    void getRelatedEntities_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            relationController.getRelatedEntities(
                sourceEntityType, sourceEntityId, targetEntityType
            )
        );
        
        assertEquals("Failed to get related entities: " + errorMessage, exception.getMessage());
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );
    }
    
    @Test
    void createInternalRelation_shouldReturnLong() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        Long expectedRelationId = 789L;

        when(relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenReturn(expectedRelationId);

        // When
        Long result = relationController.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );

        // Then
        assertEquals(expectedRelationId, result);
        verify(relationService).createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void createInternalRelation_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            relationController.createInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
            )
        );
        
        assertEquals("Failed to create relation: " + errorMessage, exception.getMessage());
        verify(relationService).createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelation_shouldReturnBoolean() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;

        when(relationService.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenReturn(true);

        // When
        boolean result = relationController.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );

        // Then
        assertTrue(result);
        verify(relationService).deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelation_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            relationController.deleteInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
            )
        );
        
        assertEquals("Failed to delete relation: " + errorMessage, exception.getMessage());
        verify(relationService).deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelationById_shouldReturnBoolean() {
        // Given
        Long relationId = 123L;

        when(relationService.deleteInternalRelationById(relationId)).thenReturn(true);

        // When
        boolean result = relationController.deleteInternalRelationById(relationId);

        // Then
        assertTrue(result);
        verify(relationService).deleteInternalRelationById(relationId);
    }

    @Test
    void deleteInternalRelationById_whenExceptionThrown_shouldThrowDataAccessException() {
        // Given
        Long relationId = 123L;
        String errorMessage = "Test error";

        when(relationService.deleteInternalRelationById(relationId)).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        DataAccessException exception = assertThrows(DataAccessException.class, () -> 
            relationController.deleteInternalRelationById(relationId)
        );
        
        assertEquals("Failed to delete relation: " + errorMessage, exception.getMessage());
        verify(relationService).deleteInternalRelationById(relationId);
    }
}
