package yurykorzun.art.universe.music.data.approved.service;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.dto.EntityDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationBindingDTO;
import yurykorzun.art.universe.music.data.approved.dto.RelationPair;
import yurykorzun.art.universe.music.data.approved.entity.Artist;
import yurykorzun.art.universe.music.data.approved.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.approved.entity.Category;
import yurykorzun.art.universe.music.data.approved.entity.CategoryBinding;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;
import yurykorzun.art.universe.music.data.approved.relation.RelationRegistry;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Tag("integration")
@Import({
    RelationRegistry.class,
})
class RelationServiceIntegrationTest extends JpaOnlyTest {

    @Autowired
    private TestEntityManager testEntityManager;
    
    @Autowired
    private EntityManager entityManager;
    
    @Autowired
    private RelationRegistry relationRegistry;
    
    private RelationServiceImpl relationService;
    
    private Artist artist1;
    private Artist artist2;
    private Category category1;
    private Category category2;
    
    @BeforeEach
    void setUp() {
        relationService = new RelationServiceImpl(entityManager, relationRegistry);
        
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
            .referenceId(artist1.getId())
            .build();
        
        ArtistBinding artistBinding2 = ArtistBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(456L)
            .referenceId(artist2.getId())
            .build();
        
        CategoryBinding categoryBinding1 = CategoryBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(789L)
            .referenceId(category1.getId())
            .build();
        
        CategoryBinding categoryBinding2 = CategoryBinding.builder()
            .dataSource(DataSource.LASTFM)
            .externalId(101L)
            .referenceId(category2.getId())
            .build();
        
        testEntityManager.persist(artistBinding1);
        testEntityManager.persist(artistBinding2);
        testEntityManager.persist(categoryBinding1);
        testEntityManager.persist(categoryBinding2);
        
        testEntityManager.flush();
    }
    
    @Test
    void bindRelation_shouldCreateRelationAndBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 789L;
        
        // When
        RelationBindingDTO result = relationService.bindRelation(
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
    void unbindRelation_shouldRemoveBinding() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceExternalEntityId = 123L;
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetExternalEntityId = 789L;
        
        // First create a relation and binding
        relationService.bindRelation(
            dataSource, sourceEntityType, sourceExternalEntityId, targetEntityType, targetExternalEntityId);
        
        // When
        boolean result = relationService.unbindRelation(
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
    void findBoundRelations_shouldReturnBindings() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        EntityType targetEntityType = EntityType.CATEGORY;
        
        // Create relations and bindings
        relationService.bindRelation(dataSource, sourceEntityType, 123L, targetEntityType, 789L);
        relationService.bindRelation(dataSource, sourceEntityType, 456L, targetEntityType, 101L);
        
        List<RelationPair> pairs = Arrays.asList(
            new RelationPair(123L, 789L),
            new RelationPair(456L, 101L),
            new RelationPair(999L, 999L) // Non-existent pair
        );
        
        // When
        List<RelationBindingDTO> results = relationService.findBoundRelations(
            dataSource, sourceEntityType, targetEntityType, pairs);
        
        // Then
        assertThat(results).hasSize(2);
        
        assertThat(results.get(0).getSourceExternalId()).isEqualTo(123L);
        assertThat(results.get(0).getTargetExternalId()).isEqualTo(789L);
        assertThat(results.get(0).getSourceEntityName()).isEqualTo("Artist 1");
        assertThat(results.get(0).getTargetEntityName()).isEqualTo("Category 1");
        
        assertThat(results.get(1).getSourceExternalId()).isEqualTo(456L);
        assertThat(results.get(1).getTargetExternalId()).isEqualTo(101L);
        assertThat(results.get(1).getSourceEntityName()).isEqualTo("Artist 2");
        assertThat(results.get(1).getTargetEntityName()).isEqualTo("Category 2");
    }
    
    @Test
    void getRelatedEntities_shouldReturnEntities() {
        // Given
        DataSource dataSource = DataSource.LASTFM;
        EntityType sourceEntityType = EntityType.ARTIST;
        EntityType targetEntityType = EntityType.CATEGORY;
        
        // Create relations
        relationService.bindRelation(dataSource, sourceEntityType, 123L, targetEntityType, 789L);
        relationService.bindRelation(dataSource, sourceEntityType, 123L, targetEntityType, 101L);
        
        // When
        List<EntityDTO> results = relationService.getRelatedEntities(
            sourceEntityType, artist1.getId(), targetEntityType);
        
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
    void createRelation_shouldCreateRelation() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // When
        Long relationId = relationService.createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
        
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
    void createRelation_whenEntityDoesNotExist_shouldThrowException() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = 999L; // Non-existent ID
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // When & Then
        assertThrows(EntityNotFoundException.class, () -> 
            relationService.createRelation(sourceEntityType, sourceEntityId, targetEntityType, targetEntityId));
    }
    
    @Test
    void deleteRelation_shouldDeleteRelation() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // Create relation
        Long relationId = relationService.createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
        
        // When
        boolean result = relationService.deleteRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
        
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
    void deleteRelation_whenRelationDoesNotExist_shouldReturnFalse() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // When
        boolean result = relationService.deleteRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
        
        // Then
        assertThat(result).isFalse();
    }
    
    @Test
    void deleteRelationById_shouldDeleteRelation() {
        // Given
        EntityType sourceEntityType = EntityType.ARTIST;
        Long sourceEntityId = artist1.getId();
        EntityType targetEntityType = EntityType.CATEGORY;
        Long targetEntityId = category1.getId();
        
        // Create relation
        Long relationId = relationService.createRelation(
            sourceEntityType, sourceEntityId, targetEntityType, targetEntityId);
        
        // When
        boolean result = relationService.deleteRelationById(relationId);
        
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
    void deleteRelationById_whenRelationDoesNotExist_shouldReturnFalse() {
        // Given
        Long nonExistentRelationId = 999L;
        
        // When
        boolean result = relationService.deleteRelationById(nonExistentRelationId);
        
        // Then
        assertThat(result).isFalse();
    }
}
