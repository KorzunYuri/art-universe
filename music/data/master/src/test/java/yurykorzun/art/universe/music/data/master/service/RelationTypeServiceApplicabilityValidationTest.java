package yurykorzun.art.universe.music.data.master.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationType;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeApplicabilityRepository;
import yurykorzun.art.universe.music.data.master.repository.RelationTypeRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the canonical entity-type ordering validation in
 * {@link RelationTypeServiceImpl#addApplicability}.
 * <p>
 * Allowed combinations (artist=1 < album=2 < track=3):
 *   same-type: artist→artist, album→album, track→track
 *   cross-type (lower code first): artist→album, artist→track, album→track
 * Rejected:
 *   reversed cross-type: album→artist, track→artist, track→album
 *   non-relatable types in applicability: category, dimension
 */
@ExtendWith(MockitoExtension.class)
class RelationTypeServiceApplicabilityValidationTest {

    @Mock
    private RelationTypeRepository relationTypeRepository;

    @Mock
    private RelationTypeApplicabilityRepository applicabilityRepository;

    @InjectMocks
    private RelationTypeServiceImpl service;

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    /** Stubs findById only — use for tests where validation throws before any repo write. */
    private void givenRelationTypeExists(long id) {
        RelationType relationType = RelationType.builder()
                .name("Test Type")
                .symmetrical(false)
                .build();
        when(relationTypeRepository.findById(id)).thenReturn(Optional.of(relationType));
    }

    /** Stubs all repo calls — use for tests where the happy path runs to completion. */
    private void givenRelationTypeReadyToSave(long id) {
        givenRelationTypeExists(id);
        when(applicabilityRepository.existsByRelationTypeIdAndSourceEntityTypeAndTargetEntityType(
                anyLong(), any(), any())).thenReturn(false);
        when(applicabilityRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    // -----------------------------------------------------------------------
    // Valid canonical combinations — no exception expected
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "canonical {0}→{1} accepted")
    @CsvSource({
        "ARTIST, ARTIST",
        "ALBUM,  ALBUM",
        "TRACK,  TRACK",
        "ARTIST, ALBUM",
        "ARTIST, TRACK",
        "ALBUM,  TRACK",
    })
    void addApplicability_canonicalCombination_doesNotThrow(MasterEntityType source, MasterEntityType target) {
        givenRelationTypeReadyToSave(1L);

        assertThatCode(() -> service.addApplicability(1L, source, target, false))
                .doesNotThrowAnyException();
    }

    // -----------------------------------------------------------------------
    // Invalid reversed cross-type combinations — IllegalArgumentException expected
    // -----------------------------------------------------------------------

    @ParameterizedTest(name = "reversed {0}→{1} rejected")
    @CsvSource({
        "ALBUM, ARTIST",
        "TRACK, ARTIST",
        "TRACK, ALBUM",
    })
    void addApplicability_reversedCombination_throwsIllegalArgument(MasterEntityType source, MasterEntityType target) {
        givenRelationTypeExists(1L);

        assertThatThrownBy(() -> service.addApplicability(1L, source, target, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Non-canonical combination");
    }

    // -----------------------------------------------------------------------
    // Non-relatable entity types (CATEGORY, DIMENSION)
    // -----------------------------------------------------------------------

    @Test
    void addApplicability_categoryAsSource_throwsIllegalArgument() {
        givenRelationTypeExists(1L);

        assertThatThrownBy(() -> service.addApplicability(1L, MasterEntityType.CATEGORY, MasterEntityType.ARTIST, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Applicability only valid for artist, album and track");
    }

    @Test
    void addApplicability_categoryAsTarget_throwsIllegalArgument() {
        givenRelationTypeExists(1L);

        assertThatThrownBy(() -> service.addApplicability(1L, MasterEntityType.ARTIST, MasterEntityType.CATEGORY, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Applicability only valid for artist, album and track");
    }

    @Test
    void addApplicability_dimensionAsSource_throwsIllegalArgument() {
        givenRelationTypeExists(1L);

        assertThatThrownBy(() -> service.addApplicability(1L, MasterEntityType.DIMENSION, MasterEntityType.ARTIST, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Applicability only valid for artist, album and track");
    }

    // -----------------------------------------------------------------------
    // System relation type guard — validated before combination check
    // -----------------------------------------------------------------------

    @Test
    void addApplicability_systemRelationType_throwsBeforeCombinationCheck() {
        RelationType systemType = RelationType.builder()
                .name("System Type")
                .symmetrical(false)
                .system(true)
                .build();
        when(relationTypeRepository.findById(99L)).thenReturn(Optional.of(systemType));

        // Even a valid canonical combination must be rejected for system types
        assertThatThrownBy(() -> service.addApplicability(99L, MasterEntityType.ARTIST, MasterEntityType.ALBUM, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("system relation type");
    }
}
