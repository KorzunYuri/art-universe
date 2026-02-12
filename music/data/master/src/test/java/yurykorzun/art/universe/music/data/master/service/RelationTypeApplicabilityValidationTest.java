package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationRegistry;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Import({
    RelationRegistry.class,
})
class RelationTypeApplicabilityValidationTest extends BaseMasterDataJpaTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RelationRegistry relationRegistry;

    private RelationServiceImpl relationService;

    private Artist artist1;
    private Artist artist2;
    private Track track1;
    private Category category1;
    private RelationType featuringType;
    private RelationType collaborationType;

    @BeforeEach
    void setUp() {
        relationService = new RelationServiceImpl(entityManager, relationRegistry);

        // Create test entities
        artist1 = Artist.builder().name("Artist 1").build();
        artist2 = Artist.builder().name("Artist 2").build();
        testEntityManager.persist(artist1);
        testEntityManager.persist(artist2);

        track1 = Track.builder().name("Track 1").primaryArtistId(artist1.getId()).build();
        testEntityManager.persist(track1);

        category1 = Category.builder().name("Category 1").build();
        testEntityManager.persist(category1);

        // Create relation types
        featuringType = RelationType.builder()
            .name("Featuring")
            .reverseName("Featured on")
            .symmetrical(false)
            .build();
        testEntityManager.persist(featuringType);

        collaborationType = RelationType.builder()
            .name("Collaboration")
            .symmetrical(true)
            .build();
        testEntityManager.persist(collaborationType);

        // Create applicabilities:
        // "Featuring" is applicable to ARTIST-TRACK
        RelationTypeApplicability featuringArtistTrack = RelationTypeApplicability.builder()
            .relationTypeId(featuringType.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(false)
            .build();
        testEntityManager.persist(featuringArtistTrack);

        // "Collaboration" is applicable to ARTIST-ARTIST
        RelationTypeApplicability collaborationArtistArtist = RelationTypeApplicability.builder()
            .relationTypeId(collaborationType.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.ARTIST)
            .isDefault(false)
            .build();
        testEntityManager.persist(collaborationArtistArtist);

        testEntityManager.flush();
    }

    @Test
    void createInternalRelation_withApplicableType_shouldSucceed() {
        // Given: "Featuring" type is applicable to ARTIST-TRACK
        // When & Then: should succeed
        Long relationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            featuringType.getId());

        assertThat(relationId).isNotNull();

        // Verify that relation exists with the correct type
        String checkSql = "SELECT relation_type_id FROM artist_track WHERE id = ?";
        Number storedTypeId = (Number) entityManager.createNativeQuery(checkSql)
            .setParameter(1, relationId)
            .getSingleResult();

        assertThat(storedTypeId.longValue()).isEqualTo(featuringType.getId());
    }

    @Test
    void createInternalRelation_withApplicableType_reversedDirection_shouldSucceed() {
        // Given: "Featuring" is applicable to ARTIST-TRACK
        // When: creating relation as TRACK -> ARTIST (reversed)
        // Then: should still succeed since applicability is direction-independent
        Long relationId = relationService.createInternalRelation(
            MasterEntityType.TRACK, track1.getId(),
            MasterEntityType.ARTIST, artist1.getId(),
            featuringType.getId());

        assertThat(relationId).isNotNull();
    }

    @Test
    void createInternalRelation_withNonApplicableType_shouldThrow() {
        // Given: "Featuring" is only applicable to ARTIST-TRACK, not ARTIST-ALBUM
        // Create an album to test with a relation that supports types but wrong applicability
        var album1 = yurykorzun.art.universe.music.data.master.entity.Album.builder()
            .name("Album 1").primaryArtistId(artist1.getId()).build();
        testEntityManager.persist(album1);
        testEntityManager.flush();

        // When & Then: should throw IllegalArgumentException about not applicable
        assertThatThrownBy(() ->
            relationService.createInternalRelation(
                MasterEntityType.ARTIST, artist1.getId(),
                MasterEntityType.ALBUM, album1.getId(),
                featuringType.getId())
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("not applicable")
         .hasMessageContaining("artist")
         .hasMessageContaining("album");
    }

    @Test
    void createInternalRelation_withNonExistentType_shouldThrow() {
        // Given: relation type ID 999999 does not exist
        Long nonExistentTypeId = 999999L;

        // When & Then: should throw CustomEntityNotFoundException
        assertThatThrownBy(() ->
            relationService.createInternalRelation(
                MasterEntityType.ARTIST, artist1.getId(),
                MasterEntityType.TRACK, track1.getId(),
                nonExistentTypeId)
        ).isInstanceOf(CustomEntityNotFoundException.class);
    }

    @Test
    void createInternalRelation_withNullType_shouldSkipValidation() {
        // Given: null relation type ID (untyped relation)
        // When & Then: should succeed without any applicability check for both category and typed relations
        Long categoryRelationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.CATEGORY, category1.getId(),
            null);
        assertThat(categoryRelationId).isNotNull();

        Long typedRelationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            null);
        assertThat(typedRelationId).isNotNull();
    }

    @Test
    void createInternalRelation_withTypeOnCategoryRelation_shouldThrow() {
        // Given: category relations don't support relation types
        // When & Then: should throw IllegalArgumentException about not supporting types
        assertThatThrownBy(() ->
            relationService.createInternalRelation(
                MasterEntityType.ARTIST, artist1.getId(),
                MasterEntityType.CATEGORY, category1.getId(),
                featuringType.getId())
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("does not support relation types");
    }

    @Test
    void createInternalRelation_sameEntityType_withApplicableType_shouldSucceed() {
        // Given: "Collaboration" type is applicable to ARTIST-ARTIST
        // When & Then: should succeed for same-entity relation
        Long relationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.ARTIST, artist2.getId(),
            collaborationType.getId());

        assertThat(relationId).isNotNull();

        // Verify that relation exists in artist_artist table
        String checkSql = "SELECT COUNT(*) FROM artist_artist WHERE relation_type_id = ?";
        Number count = (Number) entityManager.createNativeQuery(checkSql)
            .setParameter(1, collaborationType.getId())
            .getSingleResult();

        assertThat(count.intValue()).isEqualTo(1);
    }

    @Test
    void createInternalRelation_sameEntityType_withWrongType_shouldThrow() {
        // Given: "Featuring" is only applicable to ARTIST-TRACK, not ARTIST-ARTIST
        // When & Then: should throw IllegalArgumentException
        assertThatThrownBy(() ->
            relationService.createInternalRelation(
                MasterEntityType.ARTIST, artist1.getId(),
                MasterEntityType.ARTIST, artist2.getId(),
                featuringType.getId())
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("not applicable");
    }

    @Test
    void createInternalRelation_duplicateTypedRelation_shouldReturnExistingId() {
        // Given: a typed relation already exists
        Long firstRelationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            featuringType.getId());

        // When: creating the same typed relation again
        Long secondRelationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            featuringType.getId());

        // Then: should return the existing relation ID
        assertThat(secondRelationId).isEqualTo(firstRelationId);
    }

    @Test
    void createInternalRelation_sameEntitiesDifferentTypes_shouldCreateSeparateRelations() {
        // Given: create an untyped relation
        Long untypedRelationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            null);

        // When: create a typed relation for the same entity pair
        Long typedRelationId = relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            featuringType.getId());

        // Then: should create two separate relations
        assertThat(typedRelationId).isNotEqualTo(untypedRelationId);

        // Verify both exist in database
        String countSql = "SELECT COUNT(*) FROM artist_track WHERE artist_id = ? AND track_id = ?";
        Number count = (Number) entityManager.createNativeQuery(countSql)
            .setParameter(1, artist1.getId())
            .setParameter(2, track1.getId())
            .getSingleResult();

        assertThat(count.intValue()).isEqualTo(2);
    }
}
