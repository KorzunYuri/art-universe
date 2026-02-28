package yurykorzun.art.universe.music.data.master.service;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.master.test.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.dto.relation.RelatedEntityDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.RelationBindingStatusDTO;
import yurykorzun.art.universe.music.data.master.dto.relation.TargetEntityBindingDTO;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.Category;
import yurykorzun.art.universe.music.data.master.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.entity.RelationType;
import yurykorzun.art.universe.music.data.master.entity.RelationTypeApplicability;
import yurykorzun.art.universe.music.data.master.entity.Track;
import yurykorzun.art.universe.common.exception.CustomEntityNotFoundException;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationRegistry;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Import({
    RelationRegistry.class,
    RelationQueryDispatcher.class,
})
class RelationServiceIntegrationTest extends BaseMasterDataJpaTest {

    @Autowired
    private TestEntityManager testEntityManager;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private RelationRegistry relationRegistry;

    @Autowired
    private RelationQueryDispatcher relationQueryDispatcher;

    private RelationServiceImpl relationService;
    
    private Artist artist1;
    private Artist artist2;
    private Category category1;
    private Category category2;
    
    @BeforeEach
    void setUp() {
        relationService = new RelationServiceImpl(entityManager, relationRegistry, relationQueryDispatcher);
        
        // Create test data
        artist1 = Artist.builder().name("Artist 1").build();
        artist2 = Artist.builder().name("Artist 2").build();
        category1 = Category.builder().name("Category 1").build();
        category2 = Category.builder().name("Category 2").build();
        
        testEntityManager.persist(artist1);
        testEntityManager.persist(artist2);
        testEntityManager.persist(category1);
        testEntityManager.persist(category2);
        
        // Create bindings for external entities
        ArtistBinding artistBinding1 = ArtistBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .masterId(artist1.getId())
            .build();
        
        ArtistBinding artistBinding2 = ArtistBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(456L)
            .masterId(artist2.getId())
            .build();
        
        CategoryBinding categoryBinding1 = CategoryBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(789L)
            .masterId(category1.getId())
            .build();
        
        CategoryBinding categoryBinding2 = CategoryBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(101L)
            .masterId(category2.getId())
            .build();
        
        testEntityManager.persist(artistBinding1);
        testEntityManager.persist(artistBinding2);
        testEntityManager.persist(categoryBinding1);
        testEntityManager.persist(categoryBinding2);
        
        testEntityManager.flush();
    }
    
    @Test
    void bindRelation_shouldCreateExternalRelationAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetExternalEntityId = 789L;
        
        // When
        RelationBindingDTO result = relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSourceExternalId()).isEqualTo(sourceExternalEntityId);
        assertThat(result.getTargetExternalId()).isEqualTo(targetExternalEntityId);
        assertThat(result.getDataSource()).isEqualTo(dataSource);
        assertThat(result.getRelationId()).isNotNull();
        assertThat(result.getSourceEntityName()).isEqualTo("Artist 1");
        assertThat(result.getTargetEntityName()).isEqualTo("Category 1");
        assertThat(result.getSourceEntityType()).isEqualTo(sourceEntityType);
        assertThat(result.getTargetEntityType()).isEqualTo(targetEntityType);
        
        // Verify that relation exists in database
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE artist_id = ? AND category_id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, artist1.getId())
            .setParameter(2, category1.getId())
            .getSingleResult();
        
        assertThat(relationCount.intValue()).isEqualTo(1);
        
        // Verify that binding exists in database
        String checkBindingSql = "SELECT COUNT(*) FROM artist_category_binding WHERE data_source_id = ? AND external_artist_id = ? AND external_category_id = ?";
        Number bindingCount = (Number) entityManager.createNativeQuery(checkBindingSql)
            .setParameter(1, dataSource.getCode())
            .setParameter(2, sourceExternalEntityId)
            .setParameter(3, targetExternalEntityId)
            .getSingleResult();
        
        assertThat(bindingCount.intValue()).isEqualTo(1);
    }
    
    @Test
    void unbindExternalRelation_shouldRemoveBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetExternalEntityId = 789L;
        
        // First create a relation and binding
        relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
        
        // When
        boolean result = relationService.unbindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
        
        // Then
        assertThat(result).isTrue();
        
        // Verify that binding is removed from database
        String checkBindingSql = "SELECT COUNT(*) FROM artist_category_binding WHERE data_source_id = ? AND external_artist_id = ? AND external_category_id = ?";
        Number bindingCount = (Number) entityManager.createNativeQuery(checkBindingSql)
            .setParameter(1, dataSource.getCode())
            .setParameter(2, sourceExternalEntityId)
            .setParameter(3, targetExternalEntityId)
            .getSingleResult();
        
        assertThat(bindingCount.intValue()).isEqualTo(0);
        
        // Verify that relation still exists in database
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE artist_id = ? AND category_id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, artist1.getId())
            .setParameter(2, category1.getId())
            .getSingleResult();
        
        assertThat(relationCount.intValue()).isEqualTo(1);
    }
    
    @Test
    void getRelatedEntities_shouldReturnEntities() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        
        // Create relations
        relationService.bindExternalRelation(dataSource, sourceEntityType, 123L, targetEntityType, 789L);
        relationService.bindExternalRelation(dataSource, sourceEntityType, 123L, targetEntityType, 101L);
        
        // When
        List<RelatedEntityDTO> results = relationService.getRelatedEntities(
            sourceEntityType, artist1.getId(), targetEntityType, null);
        
        // Then
        assertThat(results).hasSize(2);
        
        assertThat(results.get(0).getId()).isEqualTo(category1.getId());
        assertThat(results.get(0).getName()).isEqualTo("Category 1");
        assertThat(results.get(0).getEntityType()).isEqualTo(targetEntityType);
        
        assertThat(results.get(1).getId()).isEqualTo(category2.getId());
        assertThat(results.get(1).getName()).isEqualTo("Category 2");
        assertThat(results.get(1).getEntityType()).isEqualTo(targetEntityType);
    }
    
    @Test
    void createRelation_shouldCreateInternalRelation() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // When
        Long relationId = relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null);
        
        // Then
        assertThat(relationId).isNotNull();
        
        // Verify that relation exists in database
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE artist_id = ? AND category_id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, sourceEntityId)
            .setParameter(2, targetEntityId)
            .getSingleResult();
        
        assertThat(relationCount.intValue()).isEqualTo(1);
    }
    
    @Test
    void createInternalRelation_whenEntityDoesNotExist_shouldThrowException() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = 999L; // Non-existent ID
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // When & Then
        assertThrows(CustomEntityNotFoundException.class, () ->
            relationService.createInternalRelation(sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null));
    }
    
    @Test
    void deleteRelation_shouldDeleteInternalRelation() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // Create relation
        Long relationId = relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null);
        
        // When
        boolean result = relationService.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null);
        
        // Then
        assertThat(result).isTrue();
        
        // Verify that relation is removed from database
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE artist_id = ? AND category_id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, sourceEntityId)
            .setParameter(2, targetEntityId)
            .getSingleResult();
        
        assertThat(relationCount.intValue()).isEqualTo(0);
    }
    
    @Test
    void deleteRelation_whenInternalRelationDoesNotExist_shouldReturnFalse() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // When
        boolean result = relationService.deleteInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void deleteRelationById_shouldDeleteInternalInternalRelation() {
        // Given
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // Create relation
        Long relationId = relationService.createInternalRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId, null);
        
        // When
        boolean result = relationService.deleteInternalRelationById(relationId);
        
        // Then
        assertThat(result).isTrue();
        
        // Verify that relation is removed from database
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, relationId)
            .getSingleResult();
        
        assertThat(relationCount.intValue()).isEqualTo(0);
    }
    
    @Test
    void deleteInternalRelationById_withNonExistentId_shouldReturnFalse() {
        // Given
        Long nonExistentRelationId = 999999L;
        
        // When
        boolean result = relationService.deleteInternalRelationById(nonExistentRelationId);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void deleteInternalRelationById_withExternalBindings_shouldCascadeDeleteBindings() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetExternalEntityId = 789L;
        
        // Create relation and binding
        RelationBindingDTO binding = relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
        Long relationId = binding.getRelationId();
        
        // Verify binding exists
        String checkBindingSql = "SELECT COUNT(*) FROM artist_category_binding WHERE master_id = ?";
        Number bindingCountBefore = (Number) entityManager.createNativeQuery(checkBindingSql)
            .setParameter(1, relationId)
            .getSingleResult();
        assertThat(bindingCountBefore.intValue()).isEqualTo(1);
        
        // When
        boolean result = relationService.deleteInternalRelationById(relationId);
        
        // Then
        assertThat(result).isTrue();
        
        // Verify relation is deleted
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, relationId)
            .getSingleResult();
        assertThat(relationCount.intValue()).isEqualTo(0);
        
        // Verify binding is cascade deleted
        Number bindingCountAfter = (Number) entityManager.createNativeQuery(checkBindingSql)
            .setParameter(1, relationId)
            .getSingleResult();
        assertThat(bindingCountAfter.intValue()).isEqualTo(0);
    }
    
    @Test
    void findBoundExternalRelations_withSourceAndTargetIds_shouldReturnBindingStatus() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(789L, 101L, 999L); // 999L doesn't exist
        
        // Create relations
        relationService.bindExternalRelation(dataSource, sourceEntityType, 123L, targetEntityType, 789L);
        
        // When
        RelationBindingStatusDTO result = relationService.findBoundExternalRelations(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSourceExternalId()).isEqualTo(sourceExternalEntityId);
        assertThat(result.getSourceEntityType()).isEqualTo(sourceEntityType);
        assertThat(result.getSourceEntityName()).isEqualTo("Artist 1");
        assertThat(result.isSourceEntityBound()).isTrue();
        assertThat(result.getTargetEntityType()).isEqualTo(targetEntityType);
        assertThat(result.getTargetBindings()).hasSize(3);
        
        // Check first target binding (should be bound both internally and externally)
        TargetEntityBindingDTO binding1 = result.getTargetBindings().get(0);
        assertThat(binding1.getTargetExternalId()).isEqualTo(789L);
        assertThat(binding1.getTargetEntityName()).isEqualTo("Category 1");
        assertThat(binding1.isTargetEntityBound()).isTrue();
        assertThat(binding1.isInternalRelationBound()).isTrue();
        assertThat(binding1.isExternalRelationBound()).isTrue();
        assertThat(binding1.getInternalRelationId()).isNotNull();
        
        // Check second target binding (bound entity but no relation)
        TargetEntityBindingDTO binding2 = result.getTargetBindings().get(1);
        assertThat(binding2.getTargetExternalId()).isEqualTo(101L);
        assertThat(binding2.getTargetEntityName()).isEqualTo("Category 2");
        assertThat(binding2.isTargetEntityBound()).isTrue();
        assertThat(binding2.isInternalRelationBound()).isFalse();
        assertThat(binding2.isExternalRelationBound()).isFalse();
        assertThat(binding2.getInternalRelationId()).isNull();
        
        // Check third target binding (doesn't exist)
        TargetEntityBindingDTO binding3 = result.getTargetBindings().get(2);
        assertThat(binding3.getTargetExternalId()).isEqualTo(999L);
        assertThat(binding3.isTargetEntityBound()).isFalse();
        assertThat(binding3.isInternalRelationBound()).isFalse();
        assertThat(binding3.isExternalRelationBound()).isFalse();
        assertThat(binding3.getInternalRelationId()).isNull();
    }
    
    @Test
    void findBoundExternalRelations_withInternalRelationButNoExternalBinding_shouldShowCorrectStatus() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(789L);
        
        // Create internal relation without external binding
        relationService.createInternalRelation(
            sourceEntityType, artist1.getId(), targetEntityType, category1.getId(), null);
        
        // When
        RelationBindingStatusDTO result = relationService.findBoundExternalRelations(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTargetBindings()).hasSize(1);
        
        // Check binding status - should have internal relation but no external binding
        TargetEntityBindingDTO binding = result.getTargetBindings().get(0);
        assertThat(binding.getTargetExternalId()).isEqualTo(789L);
        assertThat(binding.isTargetEntityBound()).isTrue();
        assertThat(binding.isInternalRelationBound()).isTrue();
        assertThat(binding.isExternalRelationBound()).isFalse();
        assertThat(binding.getInternalRelationId()).isNotNull();
    }
    
    @Test
    void findBoundExternalRelations_withNonExistentSourceEntity_shouldReturnUnboundStatus() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 999L; // Non-existent ID
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> targetExternalEntityIds = Arrays.asList(789L, 101L);
        
        // When
        RelationBindingStatusDTO result = relationService.findBoundExternalRelations(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityIds);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSourceExternalId()).isEqualTo(sourceExternalEntityId);
        assertThat(result.isSourceEntityBound()).isFalse();
        assertThat(result.getSourceInternalId()).isNull();
        assertThat(result.getTargetBindings()).isEmpty();
    }
    
    @Test
    void findBoundExternalRelations_withEmptyTargetList_shouldReturnEmptyBindings() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        List<Long> emptyTargetList = Collections.emptyList();
        
        // When
        RelationBindingStatusDTO result = relationService.findBoundExternalRelations(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, emptyTargetList);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSourceExternalId()).isEqualTo(sourceExternalEntityId);
        assertThat(result.getSourceEntityType()).isEqualTo(sourceEntityType);
        assertThat(result.getTargetEntityType()).isEqualTo(targetEntityType);
        assertThat(result.getTargetBindings()).isEmpty();
    }


    @Test
    void deleteInternalRelation_withExternalBindings_shouldCascadeDeleteBindings() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        MasterEntityType sourceEntityType = MasterEntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        MasterEntityType targetEntityType = MasterEntityType.CATEGORY;
        Long targetExternalEntityId = 789L;

        // Create relation and binding
        RelationBindingDTO binding = relationService.bindExternalRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
        Long relationId = binding.getRelationId();

        // Get internal IDs
        Long sourceInternalId = artist1.getId();
        Long targetInternalId = category1.getId();

        // Verify binding exists
        String checkBindingSql = "SELECT COUNT(*) FROM artist_category_binding WHERE master_id = ?";
        Number bindingCountBefore = (Number) entityManager.createNativeQuery(checkBindingSql)
            .setParameter(1, relationId)
            .getSingleResult();
        assertThat(bindingCountBefore.intValue()).isEqualTo(1);

        // When
        boolean result = relationService.deleteInternalRelation(
            sourceEntityType, sourceInternalId, targetEntityType, targetInternalId, null);

        // Then
        assertThat(result).isTrue();

        // Verify relation is deleted
        String checkRelationSql = "SELECT COUNT(*) FROM artist_category WHERE id = ?";
        Number relationCount = (Number) entityManager.createNativeQuery(checkRelationSql)
            .setParameter(1, relationId)
            .getSingleResult();
        assertThat(relationCount.intValue()).isEqualTo(0);

        // Verify binding is cascade deleted
        Number bindingCountAfter = (Number) entityManager.createNativeQuery(checkBindingSql)
            .setParameter(1, relationId)
            .getSingleResult();
        assertThat(bindingCountAfter.intValue()).isEqualTo(0);
    }

    // === Tests for supportsRelationTypes behavior ===

    @Test
    void getRelatedEntities_forCategoryRelation_shouldReturnWithoutRelationType() {
        // Given: category relations don't support types
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.CATEGORY, category1.getId(), null);
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.CATEGORY, category2.getId(), null);

        // When
        List<RelatedEntityDTO> results = relationService.getRelatedEntities(
            MasterEntityType.ARTIST, artist1.getId(), MasterEntityType.CATEGORY, null);

        // Then: should return entities without relation type info
        assertThat(results).hasSize(2);
        assertThat(results).allSatisfy(dto -> {
            assertThat(dto.getRelationTypeId()).isNull();
            assertThat(dto.getRelationTypeName()).isNull();
        });
    }

    @Test
    void getRelatedEntities_forTypedRelation_shouldReturnWithRelationType() {
        // Given: create track and typed relation
        Track track1 = Track.builder().name("Track 1").primaryArtistId(artist1.getId()).build();
        testEntityManager.persist(track1);

        RelationType featuringType = RelationType.builder()
            .name("Featuring").reverseName("Featured on").symmetrical(false).build();
        testEntityManager.persist(featuringType);

        RelationTypeApplicability applicability = RelationTypeApplicability.builder()
            .relationTypeId(featuringType.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(false)
            .build();
        testEntityManager.persist(applicability);
        testEntityManager.flush();

        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            featuringType.getId());

        // When
        List<RelatedEntityDTO> results = relationService.getRelatedEntities(
            MasterEntityType.ARTIST, artist1.getId(), MasterEntityType.TRACK, null);

        // Then: should return entity with relation type info
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRelationTypeId()).isEqualTo(featuringType.getId());
        assertThat(results.get(0).getRelationTypeName()).isEqualTo("Featuring");
    }

    @Test
    void getRelatedEntities_forTypedRelation_filterByType_shouldReturnOnlyMatching() {
        // Given: create track and two typed relations
        Track track1 = Track.builder().name("Track 1").primaryArtistId(artist1.getId()).build();
        testEntityManager.persist(track1);

        RelationType featuringType = RelationType.builder()
            .name("Featuring2").reverseName("Featured on").symmetrical(false).build();
        testEntityManager.persist(featuringType);

        RelationType remixType = RelationType.builder()
            .name("Remix2").symmetrical(false).build();
        testEntityManager.persist(remixType);

        testEntityManager.persist(RelationTypeApplicability.builder()
            .relationTypeId(featuringType.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(false).build());
        testEntityManager.persist(RelationTypeApplicability.builder()
            .relationTypeId(remixType.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(false).build());
        testEntityManager.flush();

        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            featuringType.getId());
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(),
            remixType.getId());

        // When: filter by featuringType
        List<RelatedEntityDTO> results = relationService.getRelatedEntities(
            MasterEntityType.ARTIST, artist1.getId(), MasterEntityType.TRACK, featuringType.getId());

        // Then: should return only the featuring relation
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getRelationTypeName()).isEqualTo("Featuring2");
    }

    @Test
    void deleteInternalRelation_forCategoryRelation_shouldWorkWithNullType() {
        // Given: category relation (no type support)
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.CATEGORY, category1.getId(), null);

        // When
        boolean result = relationService.deleteInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.CATEGORY, category1.getId(), null);

        // Then
        assertThat(result).isTrue();

        String checkSql = "SELECT COUNT(*) FROM artist_category WHERE artist_id = ? AND category_id = ?";
        Number count = (Number) entityManager.createNativeQuery(checkSql)
            .setParameter(1, artist1.getId())
            .setParameter(2, category1.getId())
            .getSingleResult();
        assertThat(count.intValue()).isEqualTo(0);
    }

    @Test
    void deleteInternalRelation_forTypedRelation_shouldDeleteSpecificType() {
        // Given: create two typed relations for the same entity pair
        Track track1 = Track.builder().name("Track 1").primaryArtistId(artist1.getId()).build();
        testEntityManager.persist(track1);

        RelationType type1 = RelationType.builder().name("Type1").symmetrical(false).build();
        RelationType type2 = RelationType.builder().name("Type2").symmetrical(false).build();
        testEntityManager.persist(type1);
        testEntityManager.persist(type2);

        testEntityManager.persist(RelationTypeApplicability.builder()
            .relationTypeId(type1.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(false).build());
        testEntityManager.persist(RelationTypeApplicability.builder()
            .relationTypeId(type2.getId())
            .sourceEntityType(MasterEntityType.ARTIST)
            .targetEntityType(MasterEntityType.TRACK)
            .isDefault(false).build());
        testEntityManager.flush();

        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(), type1.getId());
        relationService.createInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(), type2.getId());

        // When: delete only type1 relation
        boolean result = relationService.deleteInternalRelation(
            MasterEntityType.ARTIST, artist1.getId(),
            MasterEntityType.TRACK, track1.getId(), type1.getId());

        // Then: type1 deleted, type2 remains
        assertThat(result).isTrue();

        String countSql = "SELECT COUNT(*) FROM artist_track WHERE artist_id = ? AND track_id = ?";
        Number count = (Number) entityManager.createNativeQuery(countSql)
            .setParameter(1, artist1.getId())
            .setParameter(2, track1.getId())
            .getSingleResult();
        assertThat(count.intValue()).isEqualTo(1);

        String checkTypeSql = "SELECT relation_type_id FROM artist_track WHERE artist_id = ? AND track_id = ?";
        Number remainingTypeId = (Number) entityManager.createNativeQuery(checkTypeSql)
            .setParameter(1, artist1.getId())
            .setParameter(2, track1.getId())
            .getSingleResult();
        assertThat(remainingTypeId.longValue()).isEqualTo(type2.getId());
    }

    @Test
    void createInternalRelation_forCategoryRelation_withType_shouldThrow() {
        // Given: category relations don't support types
        RelationType someType = RelationType.builder().name("SomeType").symmetrical(false).build();
        testEntityManager.persist(someType);
        testEntityManager.flush();

        // When & Then: should reject with IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () ->
            relationService.createInternalRelation(
                MasterEntityType.ARTIST, artist1.getId(),
                MasterEntityType.CATEGORY, category1.getId(),
                someType.getId()));
    }
}
