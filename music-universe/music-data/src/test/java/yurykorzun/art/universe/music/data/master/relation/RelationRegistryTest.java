package yurykorzun.art.universe.music.data.master.relation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategory;
import yurykorzun.art.universe.music.data.master.entity.ArtistCategoryBinding;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;

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
            MasterEntityType.ARTIST, MasterEntityType.CATEGORY);

        // Then
        assertEquals(ArtistCategory.class, entityClass);
    }

    @Test
    void getRelationEntityClass_withReverseOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationEntity> entityClass = relationRegistry.getRelationEntityClass(
            MasterEntityType.CATEGORY, MasterEntityType.ARTIST);

        // Then
        assertEquals(ArtistCategory.class, entityClass);
    }

    @Test
    void getRelationEntityClass_withInvalidTypes_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            relationRegistry.getRelationEntityClass(MasterEntityType.ALBUM, MasterEntityType.TRACK));
    }

    @Test
    void getRelationBindingEntityClass_withDirectOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationBindingEntity> entityClass = relationRegistry.getRelationBindingEntityClass(
            MasterEntityType.ARTIST, MasterEntityType.CATEGORY);

        // Then
        assertEquals(ArtistCategoryBinding.class, entityClass);
    }

    @Test
    void getRelationBindingEntityClass_withReverseOrder_shouldReturnCorrectClass() {
        // When
        Class<? extends RelationBindingEntity> entityClass = relationRegistry.getRelationBindingEntityClass(
            MasterEntityType.CATEGORY, MasterEntityType.ARTIST);

        // Then
        assertEquals(ArtistCategoryBinding.class, entityClass);
    }

    @Test
    void getRelationBindingEntityClass_withInvalidTypes_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            relationRegistry.getRelationBindingEntityClass(MasterEntityType.ALBUM, MasterEntityType.TRACK));
    }

    @Test
    void isFirstEntityInRelation_withArtistCategory_shouldReturnTrue() {
        // When
        boolean result = relationRegistry.isFirstEntityInRelation(MasterEntityType.ARTIST, MasterEntityType.CATEGORY);

        // Then
        assertTrue(result);
    }

    @Test
    void isFirstEntityInRelation_withCategoryArtist_shouldReturnFalse() {
        // When
        boolean result = relationRegistry.isFirstEntityInRelation(MasterEntityType.CATEGORY, MasterEntityType.ARTIST);

        // Then
        assertFalse(result);
    }

    @Test
    void getRelationTableName_shouldReturnCorrectName() {
        // When
        String tableName = relationRegistry.getRelationTableName(MasterEntityType.ARTIST, MasterEntityType.CATEGORY);

        // Then
        assertEquals("artist_category", tableName);
    }

    @Test
    void getRelationBindingTableName_shouldReturnCorrectName() {
        // When
        String tableName = relationRegistry.getRelationBindingTableName(MasterEntityType.ARTIST, MasterEntityType.CATEGORY);

        // Then
        assertEquals("artist_category_binding", tableName);
    }
}
