package yurykorzun.art.universe.music.data.approved.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.approved.dto.EntityDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;
import yurykorzun.art.universe.music.data.approved.service.RelationService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyList;
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
    void bindExternalRelation_shouldReturnSuccessResponse() {
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

        when(relationService.bindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenReturn(expectedBinding);

        // When
        ResponseEntity<ResponseWrapper<RelationBindingDTO>> response = relationController.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBinding, response.getBody().getData());
        verify(relationService).bindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void bindExternalRelation_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.bindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<RelationBindingDTO>> response = relationController.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to bind relation: " + errorMessage, response.getBody().getMessage());
        verify(relationService).bindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void unbindExternalRelation_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;

        when(relationService.unbindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenReturn(true);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> response = relationController.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().getData());
        verify(relationService).unbindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void unbindExternalRelation_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.unbindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> response = relationController.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to unbind relation: " + errorMessage, response.getBody().getMessage());
        verify(relationService).unbindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId
        );
    }

    @Test
    void findBoundExternalRelations_shouldReturnSuccessResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        EntityType targetEntityType = EntityType.CATEGORY;
        List<String> pairs = Arrays.asList("123-456", "789-101");

        List<RelationBindingDTO> expectedBindings = Arrays.asList(
            RelationBindingDTO.builder()
                .sourceExternalId(123L)
                .targetExternalId(456L)
                .dataSource(dataSource)
                .relationId(1L)
                .sourceEntityName("Artist 1")
                .targetEntityName("Category 1")
                .sourceEntityType(sourceEntityType)
                .targetEntityType(targetEntityType)
                .build(),
            RelationBindingDTO.builder()
                .sourceExternalId(789L)
                .targetExternalId(101L)
                .dataSource(dataSource)
                .relationId(2L)
                .sourceEntityName("Artist 2")
                .targetEntityName("Category 2")
                .sourceEntityType(sourceEntityType)
                .targetEntityType(targetEntityType)
                .build()
        );

        when(relationService.findBoundRelations(
            eq(dataSource), eq(sourceEntityType), eq(targetEntityType), anyList()
        )).thenReturn(expectedBindings);

        // When
        ResponseEntity<ResponseWrapper<List<RelationBindingDTO>>> response = relationController.findBoundExternalRelations(
            dataSource, sourceEntityType, targetEntityType, pairs
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedBindings, response.getBody().getData());
        verify(relationService).findBoundRelations(
            eq(dataSource), eq(sourceEntityType), eq(targetEntityType), anyList()
        );
    }

    @Test
    void findBoundExternalRelations_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        EntityType targetEntityType = EntityType.CATEGORY;
        List<String> pairs = Arrays.asList("123-456", "789-101");
        String errorMessage = "Test error";

        when(relationService.findBoundRelations(
            eq(dataSource), eq(sourceEntityType), eq(targetEntityType), anyList()
        )).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<RelationBindingDTO>>> response = relationController.findBoundExternalRelations(
            dataSource, sourceEntityType, targetEntityType, pairs
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to get bound relations: " + errorMessage, response.getBody().getMessage());
        verify(relationService).findBoundRelations(
            eq(dataSource), eq(sourceEntityType), eq(targetEntityType), anyList()
        );
    }

    @Test
    void getRelatedEntities_shouldReturnSuccessResponse() {
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
        ResponseEntity<ResponseWrapper<List<EntityDTO>>> response = relationController.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedEntities, response.getBody().getData());
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );
    }

    @Test
    void getRelatedEntities_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        )).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<List<EntityDTO>>> response = relationController.getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to get related entities: " + errorMessage, response.getBody().getMessage());
        verify(relationService).getRelatedEntities(
            sourceEntityType, sourceEntityId, targetEntityType
        );
    }
    
    @Test
    void createInternalRelation_shouldReturnSuccessResponse() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        Long expectedRelationId = 789L;

        when(relationService.createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenReturn(expectedRelationId);

        // When
        ResponseEntity<ResponseWrapper<Long>> response = relationController.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedRelationId, response.getBody().getData());
        verify(relationService).createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void createInternalRelation_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Long>> response = relationController.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to create relation: " + errorMessage, response.getBody().getMessage());
        verify(relationService).createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelation_shouldReturnSuccessResponse() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;

        when(relationService.deleteRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenReturn(true);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> response = relationController.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().getData());
        verify(relationService).deleteRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelation_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.deleteRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        )).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> response = relationController.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to delete relation: " + errorMessage, response.getBody().getMessage());
        verify(relationService).deleteRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId
        );
    }

    @Test
    void deleteInternalRelationById_shouldReturnSuccessResponse() {
        // Given
        Long relationId = 123L;

        when(relationService.deleteRelationById(relationId)).thenReturn(true);

        // When
        ResponseEntity<ResponseWrapper<Boolean>> response = relationController.deleteInternalRelationById(relationId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(true, response.getBody().getData());
        verify(relationService).deleteRelationById(relationId);
    }

    @Test
    void deleteInternalRelationById_whenExceptionThrown_shouldReturnFailureResponse() {
        // Given
        Long relationId = 123L;
        String errorMessage = "Test error";

        when(relationService.deleteRelationById(relationId)).thenThrow(new RuntimeException(errorMessage));

        // When
        ResponseEntity<ResponseWrapper<Boolean>> response = relationController.deleteInternalRelationById(relationId);

        // Then
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Failed to delete relation: " + errorMessage, response.getBody().getMessage());
        verify(relationService).deleteRelationById(relationId);
    }
}
