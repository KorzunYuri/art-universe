package yurykorzun.art.universe.music.data.master.entity.relation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.entity.ArtistAlbum;
import yurykorzun.art.universe.music.data.master.entity.ArtistAlbumBinding;
import yurykorzun.art.universe.music.data.master.entity.ArtistArtist;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import static org.junit.jupiter.api.Assertions.*;

class RelationMetadataTest {

    private RelationRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new RelationRegistry();
        registry.init();
    }

    // --- Cross-entity relation where source is first (ARTIST, ALBUM) ---

    @Test
    void constructor_crossEntity_sourceFirst_shouldSetFieldsCorrectly() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertTrue(metadata.isSourceFirst());
        assertFalse(metadata.isSameEntityRelation());
        assertTrue(metadata.isSupportsRelationTypes());
        assertEquals("artist_album", metadata.getRelationTableName());
        assertEquals("artist_album_binding", metadata.getRelationBindingTableName());
    }

    @Test
    void getFirstEntityMetadata_crossEntity_sourceFirst_shouldReturnSourceMetadata() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals(MasterEntityType.ARTIST, metadata.getFirstEntityMetadata().getEntityType());
        assertEquals(MasterEntityType.ALBUM, metadata.getSecondEntityMetadata().getEntityType());
    }

    @Test
    void getSourceIdField_crossEntity_sourceFirst_shouldReturnArtistId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals("artist_id", metadata.getSourceIdField());
        assertEquals("album_id", metadata.getTargetIdField());
    }

    @Test
    void getFirstIdField_crossEntity_sourceFirst_shouldReturnArtistId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals("artist_id", metadata.getFirstIdField());
        assertEquals("album_id", metadata.getSecondIdField());
    }

    @Test
    void getSourceExternalIdField_crossEntity_sourceFirst_shouldReturnExternalArtistId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals("external_artist_id", metadata.getSourceExternalIdField());
        assertEquals("external_album_id", metadata.getTargetExternalIdField());
    }

    @Test
    void getFirstEntityId_crossEntity_sourceFirst_shouldReturnSourceId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals(10L, metadata.getFirstEntityId(10L, 20L));
        assertEquals(20L, metadata.getSecondEntityId(10L, 20L));
    }

    @Test
    void getFirstExternalEntityId_crossEntity_sourceFirst_shouldReturnSourceExternalId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals(100L, metadata.getFirstExternalEntityId(100L, 200L));
        assertEquals(200L, metadata.getSecondExternalEntityId(100L, 200L));
    }

    @Test
    void getRelationEntityClass_crossEntity_shouldReturnCorrectClass() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals(ArtistAlbum.class, metadata.getRelationEntityClass());
    }

    @Test
    void getRelationBindingEntityClass_crossEntity_shouldReturnCorrectClass() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ALBUM, registry);

        assertEquals(ArtistAlbumBinding.class, metadata.getRelationBindingEntityClass());
    }

    // --- Cross-entity relation where source is second (ALBUM, ARTIST) -> reversed ---

    @Test
    void constructor_crossEntity_sourceSecond_shouldSetFieldsCorrectly() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ALBUM, MasterEntityType.ARTIST, registry);

        assertFalse(metadata.isSourceFirst());
        assertFalse(metadata.isSameEntityRelation());
        assertTrue(metadata.isSupportsRelationTypes());
        assertEquals("artist_album", metadata.getRelationTableName());
        assertEquals("artist_album_binding", metadata.getRelationBindingTableName());
    }

    @Test
    void getFirstEntityMetadata_crossEntity_sourceSecond_shouldReturnTargetAsFirst() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ALBUM, MasterEntityType.ARTIST, registry);

        assertEquals(MasterEntityType.ARTIST, metadata.getFirstEntityMetadata().getEntityType());
        assertEquals(MasterEntityType.ALBUM, metadata.getSecondEntityMetadata().getEntityType());
    }

    @Test
    void getSourceIdField_crossEntity_sourceSecond_shouldReturnAlbumId() {
        // Source is ALBUM but it's second in the relation, so sourceIdField comes from target metadata
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ALBUM, MasterEntityType.ARTIST, registry);

        assertEquals("artist_id", metadata.getSourceIdField());
        assertEquals("album_id", metadata.getTargetIdField());
    }

    @Test
    void getFirstEntityId_crossEntity_sourceSecond_shouldReturnTargetId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ALBUM, MasterEntityType.ARTIST, registry);

        // sourceId=10 (ALBUM), targetId=20 (ARTIST) → first=ARTIST=targetId=20
        assertEquals(20L, metadata.getFirstEntityId(10L, 20L));
        assertEquals(10L, metadata.getSecondEntityId(10L, 20L));
    }

    @Test
    void getFirstExternalEntityId_crossEntity_sourceSecond_shouldReturnTargetExternalId() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ALBUM, MasterEntityType.ARTIST, registry);

        assertEquals(200L, metadata.getFirstExternalEntityId(100L, 200L));
        assertEquals(100L, metadata.getSecondExternalEntityId(100L, 200L));
    }

    @Test
    void getSourceExternalIdField_crossEntity_sourceSecond_shouldReturnSwapped() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ALBUM, MasterEntityType.ARTIST, registry);

        // isSourceFirst=false, so sourceExternalIdField = targetMetadata.externalIdFieldName
        assertEquals("external_artist_id", metadata.getSourceExternalIdField());
        assertEquals("external_album_id", metadata.getTargetExternalIdField());
    }

    // --- Same-entity relation (ARTIST, ARTIST) ---

    @Test
    void constructor_sameEntity_shouldSetFieldsCorrectly() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ARTIST, registry);

        assertTrue(metadata.isSourceFirst());
        assertTrue(metadata.isSameEntityRelation());
        assertTrue(metadata.isSupportsRelationTypes());
        assertEquals("artist_artist", metadata.getRelationTableName());
        assertEquals("artist_artist_binding", metadata.getRelationBindingTableName());
    }

    @Test
    void getSourceIdField_sameEntity_shouldUsePrefixedFieldName() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ARTIST, registry);

        assertEquals("source_artist_id", metadata.getSourceIdField());
        assertEquals("target_artist_id", metadata.getTargetIdField());
    }

    @Test
    void getFirstIdField_sameEntity_shouldUsePrefixedFieldName() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ARTIST, registry);

        assertEquals("source_artist_id", metadata.getFirstIdField());
        assertEquals("target_artist_id", metadata.getSecondIdField());
    }

    @Test
    void getRelationEntityClass_sameEntity_shouldReturnCorrectClass() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.ARTIST, registry);

        assertEquals(ArtistArtist.class, metadata.getRelationEntityClass());
    }

    // --- Category relation (no relation types support) ---

    @Test
    void constructor_categoryRelation_shouldNotSupportRelationTypes() {
        RelationMetadata metadata = new RelationMetadata(MasterEntityType.ARTIST, MasterEntityType.CATEGORY, registry);

        assertFalse(metadata.isSupportsRelationTypes());
        assertFalse(metadata.isSameEntityRelation());
        assertEquals("artist_category", metadata.getRelationTableName());
    }
}
