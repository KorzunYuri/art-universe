package yurykorzun.art.universe.music.data.master.relation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.EntityType;

import static org.junit.jupiter.api.Assertions.*;

class RelationRegistryTest {

    private RelationRegistry relationRegistry;

    @BeforeEach
    void setUp() {
        relationRegistry = new RelationRegistry();
        relationRegistry.init();
    }

    @Test
    void getRelationEntityClass_withDirectOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationEntity> entityClass = relationRegistry.getRelationEntityClass(
            EntityType.ARTIST, EntityType.CATEGORY);

        // Then
        assertEquals(ArtistCategory.class, entityClass);
    }

    @Test
    void getRelationEntityClass_withReverseOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationEntity> entityClass = relationRegistry.getRelationEntityClass(
            EntityType.CATEGORY, EntityType.ARTIST);

        // Then
        assertEquals(ArtistCategory.class, entityClass);
    }

    @Test
    void getRelationEntityClass_withInvalidTypes_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            relationRegistry.getRelationEntityClass(EntityType.ALBUM, EntityType.TRACK));
    }

    @Test
    void getRelationBindingEntityClass_withDirectOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationBindingEntity> entityClass = relationRegistry.getRelationBindingEntityClass(
            EntityType.ARTIST, EntityType.CATEGORY);

        // Then
        assertEquals(ArtistCategoryBinding.class, entityClass);
    }

    @Test
    void getRelationBindingEntityClass_withReverseOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationBindingEntity> entityClass = relationRegistry.getRelationBindingEntityClass(
            EntityType.CATEGORY, EntityType.ARTIST);

        // Then
        assertEquals(ArtistCategoryBinding.class, entityClass);
    }

    @Test
    void getRelationBindingEntityClass_withInvalidTypes_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            relationRegistry.getRelationBindingEntityClass(EntityType.ALBUM, EntityType.TRACK));
    }

    @Test
    void isFirstEntityInRelation_withArtistCategory_shouldReturnTrue() {
        // When
        boolean result = relationRegistry.isFirstEntityInRelation(EntityType.ARTIST, EntityType.CATEGORY);

        // Then
        assertTrue(result);
    }

    @Test
    void isFirstEntityInRelation_withCategoryArtist_shouldReturnFalse() {
        // When
        boolean result = relationRegistry.isFirstEntityInRelation(EntityType.CATEGORY, EntityType.ARTIST);

        // Then
        assertFalse(result);
    }

    @Test
    void getRelationTableName_shouldReturnCorrectName() {
        // When
        String tableName = relationRegistry.getRelationTableName(EntityType.ARTIST, EntityType.CATEGORY);

        // Then
        assertEquals("artist_category", tableName);
    }

    @Test
    void getRelationBindingTableName_shouldReturnCorrectName() {
        // When
        String tableName = relationRegistry.getRelationBindingTableName(EntityType.ARTIST, EntityType.CATEGORY);

        // Then
        assertEquals("artist_category_binding", tableName);
    }
}
