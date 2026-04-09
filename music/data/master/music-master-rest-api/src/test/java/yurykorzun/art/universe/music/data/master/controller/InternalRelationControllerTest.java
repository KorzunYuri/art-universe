package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.MasterApprovalStatus;
import yurykorzun.art.universe.music.data.master.entity.Origin;
import yurykorzun.art.universe.music.data.master.service.RelationService;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InternalRelationControllerTest {

    @Mock
    private RelationService relationService;

    @InjectMocks
    private InternalRelationController controller;

    @Test
    void createInternalRelations_shouldPassEtlOriginAndDefaultPendingStatus() {
        EntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        EntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 2L;
        List<Long> expectedIds = Arrays.asList(10L);

        when(relationService.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, Origin.ETL, MasterApprovalStatus.PENDING
        )).thenReturn(expectedIds);

        List<Long> result = controller.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, MasterApprovalStatus.PENDING
        );

        assertEquals(expectedIds, result);
        verify(relationService).createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, Origin.ETL, MasterApprovalStatus.PENDING
        );
    }

    @Test
    void createInternalRelations_withRelationTypeIds_shouldPassThem() {
        EntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        EntityType targetEntityType = MasterEntityType.ALBUM;
        Long targetEntityId = 2L;
        List<Long> relationTypeIds = Arrays.asList(100L, 200L);
        List<Long> expectedIds = Arrays.asList(10L, 20L);

        when(relationService.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            relationTypeIds, Origin.ETL, MasterApprovalStatus.PENDING
        )).thenReturn(expectedIds);

        List<Long> result = controller.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            relationTypeIds, MasterApprovalStatus.PENDING
        );

        assertEquals(expectedIds, result);
        verify(relationService).createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            relationTypeIds, Origin.ETL, MasterApprovalStatus.PENDING
        );
    }

    @Test
    void createInternalRelations_withCustomApprovalStatus_shouldPassIt() {
        EntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        EntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 2L;
        List<Long> expectedIds = Arrays.asList(10L);

        when(relationService.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, Origin.ETL, MasterApprovalStatus.APPROVED
        )).thenReturn(expectedIds);

        List<Long> result = controller.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, MasterApprovalStatus.APPROVED
        );

        assertEquals(expectedIds, result);
        verify(relationService).createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, Origin.ETL, MasterApprovalStatus.APPROVED
        );
    }

    @Test
    void createInternalRelations_whenExceptionThrown_shouldPassThrough() {
        EntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 1L;
        EntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = 2L;
        RuntimeException expected = new RuntimeException("Service error");

        when(relationService.createInternalRelations(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
            null, Origin.ETL, MasterApprovalStatus.PENDING
        )).thenThrow(expected);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
            controller.createInternalRelations(
                sourceEntityType, sourceEntityId, targetEntityType, targetEntityId,
                null, MasterApprovalStatus.PENDING
            )
        );

        assertSame(expected, exception);
    }
}
