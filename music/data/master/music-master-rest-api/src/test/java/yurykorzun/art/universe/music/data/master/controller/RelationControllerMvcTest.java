package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import yurykorzun.art.universe.music.data.master.config.WebMvcTestConfig;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;
import yurykorzun.art.universe.music.data.master.service.RelationService;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataMvcTest;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RelationController.class)
@Import(WebMvcTestConfig.class)
class RelationControllerMvcTest extends BaseMasterDataMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RelationService relationService;

    @Test
    void findBoundExternalRelations_shouldReturnBindingStatusDTO() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> ids = Arrays.asList(456L, 789L);

        RelationBindingStatusDTO status = RelationBindingStatusDTO.builder()
            .sourceExternalId(sourceExternalEntityId)
            .sourceEntityType(sourceEntityType)
            .sourceEntityName("Artist Name")
            .sourceInternalId(1L)
            .isSourceEntityBound(true)
            .targetEntityType(targetEntityType)
            .targetBindings(Arrays.asList(
                TargetEntityBindingDTO.builder()
                    .targetExternalId(ids.get(0))
                    .targetEntityName("Category 1")
                    .targetInternalId(2L)
                    .isTargetEntityBound(true)
                    .isInternalRelationBound(true)
                    .internalRelationId(10L)
                    .build(),
                TargetEntityBindingDTO.builder()
                    .targetExternalId(ids.get(1))
                    .targetEntityName("Category 2")
                    .targetInternalId(3L)
                    .isTargetEntityBound(true)
                    .isInternalRelationBound(false)
                    .internalRelationId(null)
                    .build()
            ))
            .build();

        when(relationService.findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        )).thenReturn(status);

        String expectedJson = objectMapper.writeValueAsString(status);

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
    void findBoundExternalRelations_whenExceptionThrown_shouldBeHandledByGlobalExceptionHandler() throws Exception {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/relations/bound/{dataSource}/{sourceEntityType}/{sourceExternalEntityId}/{targetEntityType}",
                dataSource.name(), sourceEntityType.getName(), sourceExternalEntityId, targetEntityType.getName())
                .param("ids", "456", "789"))
            .andDo(print())
            .andExpect(status().isInternalServerError());

        verify(relationService).findBoundExternalRelations(
            eq(dataSource), eq(sourceEntityType), eq(sourceExternalEntityId), eq(targetEntityType), anyList()
        );
    }

    @Test
    void getRelatedEntities_shouldReturnEntityDTOList() throws Exception {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;

        List<RelatedEntityDTO> entities = Arrays.asList(
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
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), isNull()
        )).thenReturn(entities);

        String expectedJson = objectMapper.writeValueAsString(entities);

        // When & Then
        mockMvc.perform(get("/api/v1/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).getRelatedEntities(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), isNull()
        );
    }

    @Test
    void getRelatedEntities_whenExceptionThrown_shouldBeHandledByGlobalExceptionHandler() throws Exception {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.getRelatedEntities(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), isNull()
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(get("/api/v1/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName()))
            .andDo(print())
            .andExpect(status().isInternalServerError());

        verify(relationService).getRelatedEntities(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), isNull()
        );
    }

    @Test
    void createInternalRelations_shouldReturnListOfIds() throws Exception {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        List<Long> relationIds = Arrays.asList(789L);

        when(relationService.createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId), isNull(), eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        )).thenReturn(relationIds);

        String expectedJson = objectMapper.writeValueAsString(relationIds);

        // When & Then
        mockMvc.perform(post("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(expectedJson));

        verify(relationService).createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId),
            eq(targetEntityType), eq(targetEntityId),
            isNull(),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        );
    }

    @Test
    void createInternalRelations_whenExceptionThrown_shouldBeHandledByGlobalExceptionHandler() throws Exception {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId),
            eq(targetEntityType), eq(targetEntityId),
            isNull(),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(post("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isInternalServerError());

        verify(relationService).createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId),
            eq(targetEntityType), eq(targetEntityId),
            isNull(),
            eq(Origin.MANUAL), eq(MasterApprovalStatus.APPROVED)
        );
    }

    @Test
    void deleteInternalRelation_shouldReturnBoolean() throws Exception {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;

        when(relationService.deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId), isNull()
        )).thenReturn(true);

        String expectedJson = "true";

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(expectedJson));

        verify(relationService).deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId), isNull()
        );
    }

    @Test
    void deleteInternalRelation_whenExceptionThrown_shouldBeHandledByGlobalExceptionHandler() throws Exception {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 456L;
        String errorMessage = "Test error";

        when(relationService.deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId), isNull()
        )).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isInternalServerError());

        verify(relationService).deleteInternalRelation(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId), isNull()
        );
    }

    @Test
    void deleteInternalRelationById_shouldReturnBoolean() throws Exception {
        // Given
        Long relationId = 123L;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;

        when(relationService.deleteInternalRelationById(eq(relationId), eq(sourceEntityType), eq(targetEntityType))).thenReturn(true);

        String expectedJson = "true";

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{sourceEntityType}/{targetEntityType}/{relationId}",
                    sourceEntityType, targetEntityType, relationId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().string(expectedJson));

        verify(relationService).deleteInternalRelationById(eq(relationId), eq(sourceEntityType), eq(targetEntityType));
    }

    @Test
    void deleteInternalRelationById_whenExceptionThrown_shouldBeHandledByGlobalExceptionHandler() throws Exception {
        // Given
        Long relationId = 123L;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        String errorMessage = "Test error";

        when(relationService.deleteInternalRelationById(eq(relationId), eq(sourceEntityType), eq(targetEntityType))).thenThrow(new RuntimeException(errorMessage));

        // When & Then
        mockMvc.perform(delete("/api/v1/relations/internal/{sourceEntityType}/{targetEntityType}/{relationId}",
                    sourceEntityType, targetEntityType, relationId))
            .andDo(print())
            .andExpect(status().isInternalServerError());

        verify(relationService).deleteInternalRelationById(eq(relationId), eq(sourceEntityType), eq(targetEntityType));
    }
}
