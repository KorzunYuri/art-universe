package yurykorzun.art.universe.music.data.master.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeApplicabilityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeWithApplicabilitiesDTO;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.service.RelationTypeService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelationTypeControllerTest {

    @Mock
    private RelationTypeService relationTypeService;

    @InjectMocks
    private RelationTypeController controller;

    @Test
    void findRelationTypes_shouldDelegateToService() {
        // Given
        String search = "feat";
        List<RelationTypeDTO> expected = List.of(RelationTypeDTO.builder().id(1L).name("Featuring").build());
        when(relationTypeService.findRelationTypes(search)).thenReturn(expected);

        // When
        List<RelationTypeDTO> result = controller.findRelationTypes(search);

        // Then
        assertEquals(expected, result);
        verify(relationTypeService).findRelationTypes(search);
    }

    @Test
    void findRelationTypes_withNullSearch_shouldDelegateToService() {
        // Given
        List<RelationTypeDTO> expected = List.of(
            RelationTypeDTO.builder().id(1L).name("Featuring").build(),
            RelationTypeDTO.builder().id(2L).name("Collaboration").build()
        );
        when(relationTypeService.findRelationTypes(null)).thenReturn(expected);

        // When
        List<RelationTypeDTO> result = controller.findRelationTypes(null);

        // Then
        assertEquals(2, result.size());
        verify(relationTypeService).findRelationTypes(null);
    }

    @Test
    void getRelationType_shouldDelegateToService() {
        // Given
        Long id = 1L;
        RelationTypeWithApplicabilitiesDTO expected = RelationTypeWithApplicabilitiesDTO.builder()
            .id(id)
            .name("Featuring")
            .applicabilities(List.of())
            .build();
        when(relationTypeService.getRelationTypeWithApplicabilities(id)).thenReturn(expected);

        // When
        RelationTypeWithApplicabilitiesDTO result = controller.getRelationType(id);

        // Then
        assertEquals(expected, result);
        verify(relationTypeService).getRelationTypeWithApplicabilities(id);
    }

    @Test
    void getRelationType_whenServiceThrows_shouldPassThrough() {
        // Given
        Long id = 999L;
        RuntimeException expected = new RuntimeException("not found");
        when(relationTypeService.getRelationTypeWithApplicabilities(id)).thenThrow(expected);

        // When & Then
        RuntimeException thrown = assertThrows(RuntimeException.class, () -> controller.getRelationType(id));
        assertSame(expected, thrown);
    }

    @Test
    void getApplicableTypes_shouldDelegateToService() {
        // Given
        MasterEntityType source = MasterEntityType.ARTIST;
        MasterEntityType target = MasterEntityType.TRACK;
        List<RelationTypeDTO> expected = List.of(RelationTypeDTO.builder().id(1L).name("Featuring").build());
        when(relationTypeService.getApplicableTypes(source, target)).thenReturn(expected);

        // When
        List<RelationTypeDTO> result = controller.getApplicableTypes(source, target);

        // Then
        assertEquals(expected, result);
        verify(relationTypeService).getApplicableTypes(source, target);
    }

    @Test
    void getApplicabilities_shouldDelegateToService() {
        // Given
        Long id = 1L;
        List<RelationTypeApplicabilityDTO> expected = List.of(
            RelationTypeApplicabilityDTO.builder()
                .id(10L)
                .relationTypeId(id)
                .sourceEntityType(MasterEntityType.ARTIST)
                .targetEntityType(MasterEntityType.TRACK)
                .build()
        );
        when(relationTypeService.getApplicabilities(id)).thenReturn(expected);

        // When
        List<RelationTypeApplicabilityDTO> result = controller.getApplicabilities(id);

        // Then
        assertEquals(expected, result);
        verify(relationTypeService).getApplicabilities(id);
    }
}
