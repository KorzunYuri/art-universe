package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CrossEntityRelationTest {

    // --- ArtistAlbum ---

    @Test
    void shouldCreateArtistAlbum_whenAllMandatoryFieldsAreSet() {
        ArtistAlbum relation = ArtistAlbum.builder()
            .artistId(1L)
            .albumId(2L)
            .build();

        assertNull(relation.getId());
        assertEquals(1L, relation.getArtistId());
        assertEquals(2L, relation.getAlbumId());
        assertEquals(MasterEntityType.ARTIST, relation.getFirstEntityType());
        assertEquals(MasterEntityType.ALBUM, relation.getSecondEntityType());
        assertEquals(1L, relation.getFirstEntityId());
        assertEquals(2L, relation.getSecondEntityId());
        assertTrue(relation.supportsRelationTypes());
        assertNull(relation.getRelationTypeId());
    }

    @Test
    void shouldFailToCreateArtistAlbum_withNullArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistAlbum.builder().artistId(null));
    }

    @Test
    void shouldFailToCreateArtistAlbum_withNullAlbumId() {
        assertThrows(NullPointerException.class, () -> ArtistAlbum.builder().albumId(null));
    }

    // --- ArtistTrack ---

    @Test
    void shouldCreateArtistTrack_whenAllMandatoryFieldsAreSet() {
        ArtistTrack relation = ArtistTrack.builder()
            .artistId(1L)
            .trackId(3L)
            .build();

        assertNull(relation.getId());
        assertEquals(1L, relation.getArtistId());
        assertEquals(3L, relation.getTrackId());
        assertEquals(MasterEntityType.ARTIST, relation.getFirstEntityType());
        assertEquals(MasterEntityType.TRACK, relation.getSecondEntityType());
        assertEquals(1L, relation.getFirstEntityId());
        assertEquals(3L, relation.getSecondEntityId());
        assertTrue(relation.supportsRelationTypes());
    }

    @Test
    void shouldFailToCreateArtistTrack_withNullArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistTrack.builder().artistId(null));
    }

    @Test
    void shouldFailToCreateArtistTrack_withNullTrackId() {
        assertThrows(NullPointerException.class, () -> ArtistTrack.builder().trackId(null));
    }

    // --- AlbumTrack ---

    @Test
    void shouldCreateAlbumTrack_whenAllMandatoryFieldsAreSet() {
        AlbumTrack relation = AlbumTrack.builder()
            .albumId(2L)
            .trackId(3L)
            .trackOrder(1)
            .build();

        assertNull(relation.getId());
        assertEquals(2L, relation.getAlbumId());
        assertEquals(3L, relation.getTrackId());
        assertEquals(1, relation.getTrackOrder());
        assertEquals(MasterEntityType.ALBUM, relation.getFirstEntityType());
        assertEquals(MasterEntityType.TRACK, relation.getSecondEntityType());
        assertEquals(2L, relation.getFirstEntityId());
        assertEquals(3L, relation.getSecondEntityId());
        assertTrue(relation.supportsRelationTypes());
    }

    @Test
    void shouldFailToCreateAlbumTrack_withNullAlbumId() {
        assertThrows(NullPointerException.class, () -> AlbumTrack.builder().albumId(null));
    }

    @Test
    void shouldFailToCreateAlbumTrack_withNullTrackId() {
        assertThrows(NullPointerException.class, () -> AlbumTrack.builder().trackId(null));
    }

    @Test
    void shouldFailToCreateAlbumTrack_withNullTrackOrder() {
        assertThrows(NullPointerException.class, () -> AlbumTrack.builder().trackOrder(null));
    }

    // --- ArtistCategory (no relation types support) ---

    @Test
    void shouldCreateArtistCategory_whenAllMandatoryFieldsAreSet() {
        ArtistCategory relation = ArtistCategory.builder()
            .artistId(1L)
            .categoryId(4L)
            .build();

        assertNull(relation.getId());
        assertEquals(1L, relation.getArtistId());
        assertEquals(4L, relation.getCategoryId());
        assertEquals(MasterEntityType.ARTIST, relation.getFirstEntityType());
        assertEquals(MasterEntityType.CATEGORY, relation.getSecondEntityType());
        assertEquals(1L, relation.getFirstEntityId());
        assertEquals(4L, relation.getSecondEntityId());
        assertFalse(relation.supportsRelationTypes());
        assertNull(relation.getRelationTypeId());
    }

    @Test
    void shouldFailToCreateArtistCategory_withNullArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistCategory.builder().artistId(null));
    }

    @Test
    void shouldFailToCreateArtistCategory_withNullCategoryId() {
        assertThrows(NullPointerException.class, () -> ArtistCategory.builder().categoryId(null));
    }

    // --- AlbumCategory (no relation types support) ---

    @Test
    void shouldCreateAlbumCategory_whenAllMandatoryFieldsAreSet() {
        AlbumCategory relation = AlbumCategory.builder()
            .albumId(2L)
            .categoryId(4L)
            .build();

        assertNull(relation.getId());
        assertEquals(2L, relation.getAlbumId());
        assertEquals(4L, relation.getCategoryId());
        assertEquals(MasterEntityType.ALBUM, relation.getFirstEntityType());
        assertEquals(MasterEntityType.CATEGORY, relation.getSecondEntityType());
        assertEquals(2L, relation.getFirstEntityId());
        assertEquals(4L, relation.getSecondEntityId());
        assertFalse(relation.supportsRelationTypes());
    }

    @Test
    void shouldFailToCreateAlbumCategory_withNullAlbumId() {
        assertThrows(NullPointerException.class, () -> AlbumCategory.builder().albumId(null));
    }

    @Test
    void shouldFailToCreateAlbumCategory_withNullCategoryId() {
        assertThrows(NullPointerException.class, () -> AlbumCategory.builder().categoryId(null));
    }

    // --- TrackCategory (no relation types support) ---

    @Test
    void shouldCreateTrackCategory_whenAllMandatoryFieldsAreSet() {
        TrackCategory relation = TrackCategory.builder()
            .trackId(3L)
            .categoryId(4L)
            .build();

        assertNull(relation.getId());
        assertEquals(3L, relation.getTrackId());
        assertEquals(4L, relation.getCategoryId());
        assertEquals(MasterEntityType.TRACK, relation.getFirstEntityType());
        assertEquals(MasterEntityType.CATEGORY, relation.getSecondEntityType());
        assertEquals(3L, relation.getFirstEntityId());
        assertEquals(4L, relation.getSecondEntityId());
        assertFalse(relation.supportsRelationTypes());
    }

    @Test
    void shouldFailToCreateTrackCategory_withNullTrackId() {
        assertThrows(NullPointerException.class, () -> TrackCategory.builder().trackId(null));
    }

    @Test
    void shouldFailToCreateTrackCategory_withNullCategoryId() {
        assertThrows(NullPointerException.class, () -> TrackCategory.builder().categoryId(null));
    }
}
