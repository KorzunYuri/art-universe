package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.common.controller.ResponseWrapper;
import yurykorzun.art.universe.music.data.master.config.WebMvcTestConfig;
import yurykorzun.art.universe.music.data.master.dto.EntityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.EntityType;
import yurykorzun.art.universe.music.data.master.service.RelationService;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelationController.class)
@Import(WebMvcTestConfig.class)
class RelationControllerMvcTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RelationService relationService;

    @Test
    void bindExternalRelation_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;

        RelationBindingDTO binding = RelationBindingDTO.builder()
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
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        )).thenReturn(binding);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(binding));

        // When & Then
        mockMvc.perform(post("/api/v1/relations/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName(), targetExternalEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).bindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        );
    }

    @Test
    void bindExternalRelation_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.bindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        )).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to bind relation: " + errorMessage));

        // When & Then
        mockMvc.perform(post("/api/v1/relations/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName(), targetExternalEntityId))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).bindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        );
    }

    @Test
    void unbindExternalRelation_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;

        when(relationService.unbindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        )).thenReturn(true);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(true));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName(), targetExternalEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).unbindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        );
    }

    @Test
    void unbindExternalRelation_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.unbindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        )).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to unbind relation: " + errorMessage));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/unbind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName(), targetExternalEntityId))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).unbindExternalRelation(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), eq(targetExternalEntityId)
        );
    }

    @Test
    void findBoundExternalRelations_shouldReturnSuccessResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        List<Long> ids = Arrays.asList(456L, 789L);

        RelationBindingStatusDTO status = RelationBindingStatusDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .sourceEntityType(sourceEntityType)
            .sourceEntityName("Artist Name")
            .sourceInternalId(1L)
            .sourceEntityBound(true)
            .targetEntityType(targetEntityType)
            .targetBindings(Arrays.asList(
                TargetEntityBindingDTO.builder()
                    .targetExternalId(ids.get(0))
                    .targetEntityName("Category 1")
                    .targetInternalId(2L)
                    .targetEntityBound(true)
                    .internalRelationBound(true)
                    .externalRelationBound(true)
                    .internalRelationId(10L)
                    .build(),
                TargetEntityBindingDTO.builder()
                    .targetExternalId(ids.get(1))
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
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        )).thenReturn(status);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(status));

        // When & Then
        mockMvc.perform(get("/api/v1/relations/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName())
                .param("ids", String.valueOf(ids.get(0)), String.valueOf(ids.get(1))))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        );
    }

    @Test
    void findBoundExternalRelations_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        )).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to get bound relations: " + errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/relations/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName())
                .param("ids", "456", "789"))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        );
    }

    @Test
    void getRelatedEntities_shouldReturnSuccessResponse() throws Exception {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;

        List<EntityDTO> entities = Arrays.asList(
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
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType)
        )).thenReturn(entities);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(entities));

        // When & Then
        mockMvc.perform(get("/api/v1/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).getRelatedEntities(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType)
        );
    }

    @Test
    void getRelatedEntities_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.getRelatedEntities(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType)
        )).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to get related entities: " + errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName()))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).getRelatedEntities(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType)
        );
    }

    @Test
    void createInternalRelation_shouldReturnSuccessResponse() throws Exception {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        Long relationId = 789L;

        when(relationService.createInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        )).thenReturn(relationId);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(relationId));

        // When & Then
        mockMvc.perform(post("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).createInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        );
    }

    @Test
    void createInternalRelation_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.createInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        )).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to create relation: " + errorMessage));

        // When & Then
        mockMvc.perform(post("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).createInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        );
    }
    
    @Test
    void deleteInternalRelation_shouldReturnSuccessResponse() throws Exception {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;

        when(relationService.deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        )).thenReturn(true);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(true));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        );
    }

    @Test
    void deleteInternalRelation_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        )).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to delete relation: " + errorMessage));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId)
        );
    }
    
    @Test
    void deleteInternalRelationById_shouldReturnSuccessResponse() throws Exception {
        // Given
        Long relationId = 123L;

        when(relationService.deleteInternalRelationById(eq(relationId))).thenReturn(true);

        String expectedJson = objectMapper.writeValueAsString(ResponseWrapper.successBody(true));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{relationId}", relationId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).deleteInternalRelationById(eq(relationId));
    }

    @Test
    void deleteInternalRelationById_whenExceptionThrown_shouldReturnFailureResponse() throws Exception {
        // Given
        Long relationId = 123L;
        String errorMessage = "Test error";

        when(relationService.deleteInternalRelationById(eq(relationId))).thenThrow(new RuntimeException(errorMessage));

        String expectedJson = objectMapper.writeValueAsString(
            ResponseWrapper.failureBody("Failed to delete relation: " + errorMessage));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{relationId}", relationId))
            .andDo(print())
            .andExpect(status().isInternalServerError())
            .andExpect(content().json(expectedJson));

        verify(relationService).deleteInternalRelationById(eq(relationId));
    }
    
    @Test
    void bindExternalRelation_withInvalidEntityType_shouldReturnBadRequest() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        String invalidEntityType = "invalid";
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 456L;

        // When & Then
        mockMvc.perform(post("/api/v1/relations/bind/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}/{targetExternalEntityId}",
                dataSource.name(), invalidEntityType, sourceExternalEntityId, targetEntityType.getName(), targetExternalEntityId))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}
