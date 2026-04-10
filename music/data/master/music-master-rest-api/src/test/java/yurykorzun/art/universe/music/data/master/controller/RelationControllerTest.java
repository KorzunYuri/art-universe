package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.model.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.model.Origin;
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
                    .internalRelationId(10L)
                    .build(),
                TargetEntityBindingDTO.builder()
                    .targetExternalId(789L)
                    .targetEntityName("Category 2")
                    .targetInternalId(3L)
                    .isTargetEntityBound(true)
                    .isInternalRelationBound(false)
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
            sourceEntityType, sourceEntityId, targetEntityType, null
        )).thenReturn(expectedEntities);

        // When
        List<RelatedEntityDTO> result = relationController.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType, null
        );

        // Then
        assertEquals(expectedEntities, result);
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType, null
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
            sourceEntityType, sourceEntityId, targetEntityType, null
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            relationController.getRelatedEntities(
                sourceEntityType, sourceEntityId, targetEntityType, null
            )
        );

        assertSame(expectedException, exception);
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType, null
        );
    }

    @Test
    void createInternalRelations_shouldReturnListOfIds() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        List<Long> expectedRelationIds = Arrays.asList(789L);

        when(relationService.createInternalRelations(
            sourceEntityType, sourceEntityId,
            targetEntityType, targetEntityId,
            null,
            Origin.MANUAL, MasterApprovalStatus.APPROVED
        )).thenReturn(expectedRelationIds);

        // When
        List<Long> result = relationController.createInternalRelations(
            sourceEntityType, sourceEntityId,
            targetEntityType, targetEntityId,
            null
        );

        // Then
        assertEquals(expectedRelationIds, result);
        verify(relationService).createInternalRelations(
            sourceEntityType, sourceEntityId,
            targetEntityType, targetEntityId,
            null,
            Origin.MANUAL, MasterApprovalStatus.APPROVED
        );
    }

    @Test
    void createInternalRelations_whenExceptionThrown_shouldPassThroughException() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.createInternalRelations(
            sourceEntityType, sourceEntityId,
            targetEntityType, targetEntityId,
            null,
            Origin.MANUAL, MasterApprovalStatus.APPROVED
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            relationController.createInternalRelations(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
            )
        );

        assertSame(expectedException, exception);
        verify(relationService).createInternalRelations(
            sourceEntityType, sourceEntityId,
            targetEntityType, targetEntityId,
            null,
            Origin.MANUAL, MasterApprovalStatus.APPROVED
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
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
        )).thenReturn(true);

        // When
        boolean result = relationController.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
        );

        // Then
        assertTrue(result);
        verify(relationService).deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
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
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
        )).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            relationController.deleteInternalRelation(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
            )
        );

        assertSame(expectedException, exception);
        verify(relationService).deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null
        );
    }

    @Test
    void deleteInternalRelationById_shouldReturnBoolean() {
        // Given
        Long relationId = 123L;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;

        when(relationService.deleteInternalRelationById(relationId, sourceEntityType, targetEntityType)).thenReturn(true);

        // When
        boolean result = relationController.deleteInternalRelationById(sourceEntityType, targetEntityType, relationId);

        // Then
        assertTrue(result);
        verify(relationService).deleteInternalRelationById(relationId, sourceEntityType, targetEntityType);
    }

    @Test
    void deleteInternalRelationById_whenExceptionThrown_shouldPassThroughException() {
        // Given
        Long relationId = 123L;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        RuntimeException expectedException = new RuntimeException("Test error");

        when(relationService.deleteInternalRelationById(relationId, sourceEntityType, targetEntityType)).thenThrow(expectedException);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            relationController.deleteInternalRelationById(sourceEntityType, targetEntityType, relationId)
        );

        assertSame(expectedException, exception);
        verify(relationService).deleteInternalRelationById(relationId, sourceEntityType, targetEntityType);
    }
}
