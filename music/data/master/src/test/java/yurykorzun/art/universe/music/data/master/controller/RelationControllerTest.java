package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
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
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
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
    void bindExternalRelation_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.bindExternalRelation(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
            )
        );
        
        assertSame(expectedException, exception);
        verify(relationService).bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void unbindExternalRelation_shouldReturnBoolean() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
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
    void unbindExternalRelation_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.unbindExternalRelation(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
            )
        );
        
        assertSame(expectedException, exception);
        verify(relationService).unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void findBoundExternalRelations_shouldReturnBindingStatusDTO() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(456L, 789L);

        RelationBindingStatusDTO expectedStatus = RelationBindingStatusDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .sourceEntityType(sourceEntityType)
            .sourceEntityName("Artist Name")
            .sourceInternalId(1L)
            .isSourceEntityBound(true)
            .targetEntityType(targetEntityType)
            .targetBindings(Arrays.asList(
                TargetEntityBindingDTO.builder()
                    .targetExternalId(456L)
                    .targetEntityName("Category 1")
                    .targetInternalId(2L)
                    .isTargetEntityBound(true)
                    .isInternalRelationBound(true)
                    .isExternalRelationBound(true)
                    .internalRelationId(10L)
                    .build(),
                TargetEntityBindingDTO.builder()
                    .targetExternalId(789L)
                    .targetEntityName("Category 2")
                    .targetInternalId(3L)
                    .isTargetEntityBound(true)
                    .isInternalRelationBound(false)
                    .isExternalRelationBound(false)
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
    void findBoundExternalRelations_whenExceptionThrown_shouldPassThroughException() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(456L, 789L);
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityIds)
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.findBoundExternalRelations(
                dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds
            )
        );
        
        assertSame(expectedException, exception);
        verify(relationService).findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityIds)
        );
    }

    @Test
    void getRelatedEntities_shouldReturnEntityDTOList() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;

        List<RelatedEntityDTO> expectedEntities = Arrays.asList(
            RelatedEntityDTO.builder()
                .id(1L)
                .name("Category 1")
                .entityType(targetEntityType)
                .build(),
            RelatedEntityDTO.builder()
                .id(2L)
                .name("Category 2")
                .entityType(targetEntityType)
                .build()
        );

        when(relationService.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        )).thenReturn(expectedEntities);

        // When
        List<RelatedEntityDTO> result = relationController.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );

        // Then
        assertEquals(expectedEntities, result);
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );
    }

    @Test
    void getRelatedEntities_whenExceptionThrown_shouldPassThroughException() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.getRelatedEntities(
                sourceEntityType, sourceEntityId, targetEntityType
            )
        );
        
        assertSame(expectedException, exception);
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );
    }
    
    @Test
    void createInternalRelation_shouldReturnLong() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
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
    void createInternalRelation_whenExceptionThrown_shouldPassThroughException() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.createInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
            )
        );
        
        assertSame(expectedException, exception);
        verify(relationService).createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelation_shouldReturnBoolean() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
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
    void deleteInternalRelation_whenExceptionThrown_shouldPassThroughException() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.deleteInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
            )
        );
        
        assertSame(expectedException, exception);
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
    void deleteInternalRelationById_whenExceptionThrown_shouldPassThroughException() {
        // Given
        Long relationId = 123L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.deleteInternalRelationById(relationId)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            relationController.deleteInternalRelationById(relationId)
        );
        
        assertSame(expectedException, exception);
        verify(relationService).deleteInternalRelationById(relationId);
    }
}
