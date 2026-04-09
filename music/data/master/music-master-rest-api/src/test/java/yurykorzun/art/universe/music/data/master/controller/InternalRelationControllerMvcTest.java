package yurykorzun.art.universe.music.data.master.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(InternalRelationController.class)
class InternalRelationControllerMvcTest extends BaseMasterDataMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RelationService relationService;

    @Test
    void createInternalRelations_withDefaultApprovalStatus_shouldReturnListOfIds() throws Exception {
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 2L;
        List<Long> relationIds = Arrays.asList(10L);

        when(relationService.createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId),
            isNull(), eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)
        )).thenReturn(relationIds);

        mockMvc.perform(post("/api/v1/internal/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(relationIds)));

        verify(relationService).createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId),
            isNull(), eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)
        );
    }

    @Test
    void createInternalRelations_withCustomApprovalStatus_shouldPassIt() throws Exception {
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        MasterEntityType targetEntityType = MasterEntityType.ALBUM;
        Long targetEntityId = 2L;
        List<Long> relationIds = Arrays.asList(10L);

        when(relationService.createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId),
            isNull(), eq(Origin.ETL), eq(MasterApprovalStatus.APPROVED)
        )).thenReturn(relationIds);

        mockMvc.perform(post("/api/v1/internal/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId)
                .param("approvalStatus", "approved"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(relationIds)));

        verify(relationService).createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId),
            isNull(), eq(Origin.ETL), eq(MasterApprovalStatus.APPROVED)
        );
    }

    @Test
    void createInternalRelations_withRelationTypeIds_shouldPassThem() throws Exception {
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        MasterEntityType targetEntityType = MasterEntityType.ALBUM;
        Long targetEntityId = 2L;
        List<Long> relationTypeIds = Arrays.asList(100L, 200L);
        List<Long> relationIds = Arrays.asList(10L, 20L);

        when(relationService.createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId),
            eq(relationTypeIds), eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)
        )).thenReturn(relationIds);

        mockMvc.perform(post("/api/v1/internal/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId)
                .param("relationTypeIds", "100", "200"))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(relationIds)));
    }

    @Test
    void createInternalRelations_whenError_shouldReturnInternalServerError() throws Exception {
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 2L;

        when(relationService.createInternalRelations(
            eq(sourceEntityType), eq(sourceEntityId), eq(targetEntityType), eq(targetEntityId),
            isNull(), eq(Origin.ETL), eq(MasterApprovalStatus.PENDING)
        )).thenThrow(new RuntimeException("Service error"));

        mockMvc.perform(post("/api/v1/internal/relations/{sourceEntityType}/{sourceEntityId}/{targetEntityType}/{targetEntityId}",
                sourceEntityType.getName(), sourceEntityId, targetEntityType.getName(), targetEntityId))
            .andExpect(status().isInternalServerError());
    }
}
