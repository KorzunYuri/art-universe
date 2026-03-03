package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeApplicabilityDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeDTO;
import yurykorzun.art.universe.music.data.master.dto.RelationTypeWithApplicabilitiesDTO;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeApplicabilityRepository;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RelationTypeServiceImplTest {

    @Mock
    private RelationTypeRepository relationTypeRepository;

    @Mock
    private RelationTypeApplicabilityRepository applicabilityRepository;

    @InjectMocks
    private RelationTypeServiceImpl service;

    // --- findRelationTypes ---

    @Test
    void findRelationTypes_withSearch_shouldCallFindByNameContaining() {
        // Given
        String search = "feat";
        when(relationTypeRepository.findByNameContainingIgnoreCase(search))
            .thenReturn(List.of(relationType(1L, "Featuring", "Featured on", false, false)));

        // When
        List<RelationTypeDTO> result = service.findRelationTypes(search);

        // Then
        assertEquals(1, result.size());
        verify(relationTypeRepository).findByNameContainingIgnoreCase(search);
        verify(relationTypeRepository, never()).findAll();
    }

    @Test
    void findRelationTypes_withNullSearch_shouldCallFindAll() {
        // Given
        when(relationTypeRepository.findAll()).thenReturn(List.of(relationType(1L, "Featuring", null, false, false)));

        // When
        service.findRelationTypes(null);

        // Then
        verify(relationTypeRepository).findAll();
        verify(relationTypeRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void findRelationTypes_withBlankSearch_shouldCallFindAll() {
        // Given
        when(relationTypeRepository.findAll()).thenReturn(List.of());

        // When
        service.findRelationTypes("   ");

        // Then
        verify(relationTypeRepository).findAll();
        verify(relationTypeRepository, never()).findByNameContainingIgnoreCase(any());
    }

    @Test
    void findRelationTypes_shouldMapAllFieldsToDTO() {
        // Given
        when(relationTypeRepository.findAll())
            .thenReturn(List.of(relationType(5L, "Collab", "Collaborated on", true, true)));

        // When
        RelationTypeDTO dto = service.findRelationTypes(null).get(0);

        // Then
        assertEquals(5L, dto.getId());
        assertEquals("Collab", dto.getName());
        assertEquals("Collaborated on", dto.getReverseName());
        assertTrue(dto.isSymmetrical());
        assertTrue(dto.isSystem());
    }

    // --- getRelationTypeWithApplicabilities ---

    @Test
    void getRelationTypeWithApplicabilities_whenFound_shouldReturnWithApplicabilities() {
        // Given
        Long id = 1L;
        when(relationTypeRepository.findById(id))
            .thenReturn(Optional.of(relationType(id, "Featuring", "Featured on", false, false)));
        when(applicabilityRepository.findByRelationTypeId(id))
            .thenReturn(List.of(applicability(10L, id, MasterEntityType.ARTIST, MasterEntityType.TRACK, false)));

        // When
        RelationTypeWithApplicabilitiesDTO result = service.getRelationTypeWithApplicabilities(id);

        // Then
        assertEquals(id, result.getId());
        assertEquals("Featuring", result.getName());
        assertEquals(1, result.getApplicabilities().size());
        RelationTypeApplicabilityDTO app = result.getApplicabilities().get(0);
        assertEquals(10L, app.getId());
        assertEquals(id, app.getRelationTypeId());
        assertEquals(MasterEntityType.ARTIST, app.getSourceEntityType());
        assertEquals(MasterEntityType.TRACK, app.getTargetEntityType());
        assertFalse(app.isDefault());
    }

    @Test
    void getRelationTypeWithApplicabilities_withNoApplicabilities_shouldReturnEmptyList() {
        // Given
        Long id = 2L;
        when(relationTypeRepository.findById(id))
            .thenReturn(Optional.of(relationType(id, "Collab", null, true, false)));
        when(applicabilityRepository.findByRelationTypeId(id)).thenReturn(List.of());

        // When
        RelationTypeWithApplicabilitiesDTO result = service.getRelationTypeWithApplicabilities(id);

        // Then
        assertEquals(id, result.getId());
        assertTrue(result.getApplicabilities().isEmpty());
    }

    @Test
    void getRelationTypeWithApplicabilities_whenNotFound_shouldThrow() {
        // Given
        Long id = 999L;
        when(relationTypeRepository.findById(id)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(CustomEntityNotFoundException.class,
            () -> service.getRelationTypeWithApplicabilities(id));
        verify(applicabilityRepository, never()).findByRelationTypeId(any());
    }

    // --- getApplicableTypes ---

    @Test
    void getApplicableTypes_shouldReturnForwardApplicableRelationTypes() {
        // Given: "Featuring" is applicable to ARTIST→TRACK (forward direction)
        RelationType type = relationType(1L, "Featuring", "Featured on", false, false);
        RelationTypeApplicability app = applicabilityWithType(10L, 1L, type,
            MasterEntityType.ARTIST, MasterEntityType.TRACK, false);
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.TRACK))
            .thenReturn(List.of(app));
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.TRACK, MasterEntityType.ARTIST))
            .thenReturn(List.of());

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.ARTIST, MasterEntityType.TRACK);

        // Then: should return forward name
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Featuring", result.get(0).getName());
        assertEquals("Featured on", result.get(0).getReverseName());
    }

    @Test
    void getApplicableTypes_shouldReturnReverseApplicableRelationTypes() {
        // Given: "Has Member" is applicable to ARTIST→PERSON, query is PERSON→ARTIST
        RelationType type = relationType(1L, "Has Member", "Is Member Of", false, false);
        RelationTypeApplicability app = applicabilityWithType(10L, 1L, type,
            MasterEntityType.ARTIST, MasterEntityType.PERSON, false);
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.PERSON, MasterEntityType.ARTIST))
            .thenReturn(List.of());
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.PERSON))
            .thenReturn(List.of(app));

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.PERSON, MasterEntityType.ARTIST);

        // Then: should return reversed names
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals("Is Member Of", result.get(0).getName());
        assertEquals("Has Member", result.get(0).getReverseName());
    }

    @Test
    void getApplicableTypes_shouldCombineForwardAndReverse() {
        // Given: type A is applicable to X→Y (forward), type B to Y→X (reverse)
        RelationType typeA = relationType(1L, "Featuring", "Featured on", false, false);
        RelationType typeB = relationType(2L, "Produced By", "Produced", false, false);
        RelationTypeApplicability appA = applicabilityWithType(10L, 1L, typeA,
            MasterEntityType.ARTIST, MasterEntityType.TRACK, false);
        RelationTypeApplicability appB = applicabilityWithType(20L, 2L, typeB,
            MasterEntityType.TRACK, MasterEntityType.ARTIST, false);
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.TRACK))
            .thenReturn(List.of(appA));
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.TRACK, MasterEntityType.ARTIST))
            .thenReturn(List.of(appB));

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.ARTIST, MasterEntityType.TRACK);

        // Then: should contain both, with typeB reversed
        assertEquals(2, result.size());
        assertEquals("Featuring", result.get(0).getName());
        assertEquals("Produced", result.get(1).getName());
        assertEquals("Produced By", result.get(1).getReverseName());
    }

    @Test
    void getApplicableTypes_sameEntityType_shouldNotQueryReverse() {
        // Given: ARTIST→ARTIST, forward query covers both directions
        RelationType type = relationType(1L, "Similar To", "Similar To", true, false);
        RelationTypeApplicability app = applicabilityWithType(10L, 1L, type,
            MasterEntityType.ARTIST, MasterEntityType.ARTIST, false);
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.ARTIST))
            .thenReturn(List.of(app));

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.ARTIST, MasterEntityType.ARTIST);

        // Then
        assertEquals(1, result.size());
        assertEquals("Similar To", result.get(0).getName());
        // Reverse query should NOT be called for same-type pairs
        verify(applicabilityRepository, times(1))
            .findBySourceEntityTypeAndTargetEntityType(any(), any());
    }

    @Test
    void getApplicableTypes_shouldDeduplicateForwardAndReverse() {
        // Given: a type is applicable in BOTH directions (unlikely but possible)
        RelationType type = relationType(1L, "Collaborates With", null, true, false);
        RelationTypeApplicability appForward = applicabilityWithType(10L, 1L, type,
            MasterEntityType.ARTIST, MasterEntityType.TRACK, false);
        RelationTypeApplicability appReverse = applicabilityWithType(20L, 1L, type,
            MasterEntityType.TRACK, MasterEntityType.ARTIST, false);
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.TRACK))
            .thenReturn(List.of(appForward));
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.TRACK, MasterEntityType.ARTIST))
            .thenReturn(List.of(appReverse));

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.ARTIST, MasterEntityType.TRACK);

        // Then: should only include the type once (from forward match)
        assertEquals(1, result.size());
        assertEquals("Collaborates With", result.get(0).getName());
    }

    @Test
    void getApplicableTypes_whenNoneFound_shouldReturnEmptyList() {
        // Given
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.ALBUM))
            .thenReturn(List.of());
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ALBUM, MasterEntityType.ARTIST))
            .thenReturn(List.of());

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.ARTIST, MasterEntityType.ALBUM);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void getApplicableTypes_reverse_withNullReverseName_shouldFallBackToName() {
        // Given: type has no reverseName
        RelationType type = relationType(1L, "Related To", null, true, false);
        RelationTypeApplicability app = applicabilityWithType(10L, 1L, type,
            MasterEntityType.ARTIST, MasterEntityType.PERSON, false);
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.PERSON, MasterEntityType.ARTIST))
            .thenReturn(List.of());
        when(applicabilityRepository.findBySourceEntityTypeAndTargetEntityType(
            MasterEntityType.ARTIST, MasterEntityType.PERSON))
            .thenReturn(List.of(app));

        // When
        List<RelationTypeDTO> result = service.getApplicableTypes(MasterEntityType.PERSON, MasterEntityType.ARTIST);

        // Then: should fall back to name since reverseName is null
        assertEquals(1, result.size());
        assertEquals("Related To", result.get(0).getName());
        assertEquals("Related To", result.get(0).getReverseName());
    }

    // --- getApplicabilities ---

    @Test
    void getApplicabilities_shouldReturnApplicabilityDTOs() {
        // Given
        Long id = 1L;
        when(applicabilityRepository.findByRelationTypeId(id))
            .thenReturn(List.of(applicability(10L, id, MasterEntityType.ARTIST, MasterEntityType.TRACK, true)));

        // When
        List<RelationTypeApplicabilityDTO> result = service.getApplicabilities(id);

        // Then
        assertEquals(1, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(id, result.get(0).getRelationTypeId());
        assertEquals(MasterEntityType.ARTIST, result.get(0).getSourceEntityType());
        assertEquals(MasterEntityType.TRACK, result.get(0).getTargetEntityType());
        assertTrue(result.get(0).isDefault());
    }

    @Test
    void getApplicabilities_whenNone_shouldReturnEmptyList() {
        // Given
        Long id = 1L;
        when(applicabilityRepository.findByRelationTypeId(id)).thenReturn(List.of());

        // When
        List<RelationTypeApplicabilityDTO> result = service.getApplicabilities(id);

        // Then
        assertTrue(result.isEmpty());
    }

    // --- helpers ---

    private RelationType relationType(Long id, String name, String reverseName, boolean symmetrical, boolean system) {
        return RelationType.builder()
            .id(id)
            .name(name)
            .reverseName(reverseName)
            .symmetrical(symmetrical)
            .system(system)
            .build();
    }

    private RelationTypeApplicability applicability(Long id, Long typeId,
            MasterEntityType source, MasterEntityType target, boolean isDefault) {
        return RelationTypeApplicability.builder()
            .id(id)
            .relationTypeId(typeId)
            .sourceEntityType(source)
            .targetEntityType(target)
            .isDefault(isDefault)
            .build();
    }

    private RelationTypeApplicability applicabilityWithType(Long id, Long typeId, RelationType type,
            MasterEntityType source, MasterEntityType target, boolean isDefault) {
        return RelationTypeApplicability.builder()
            .id(id)
            .relationTypeId(typeId)
            .relationType(type)
            .sourceEntityType(source)
            .targetEntityType(target)
            .isDefault(isDefault)
            .build();
    }
}
