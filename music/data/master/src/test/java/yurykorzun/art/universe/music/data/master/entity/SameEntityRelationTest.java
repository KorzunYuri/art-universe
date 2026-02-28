package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SameEntityRelationTest {

    // --- ArtistArtist ---

    @Test
    void shouldCreateArtistArtist_whenAllMandatoryFieldsAreSet() {
        ArtistArtist relation = ArtistArtist.builder()
            .sourceArtistId(1L)
            .targetArtistId(2L)
            .build();

        assertNull(relation.getId());
        assertEquals(1L, relation.getSourceArtistId());
        assertEquals(2L, relation.getTargetArtistId());
        assertEquals(MasterEntityType.ARTIST, relation.getFirstEntityType());
        assertEquals(MasterEntityType.ARTIST, relation.getSecondEntityType());
        assertEquals(1L, relation.getFirstEntityId());
        assertEquals(2L, relation.getSecondEntityId());
        assertTrue(relation.supportsRelationTypes());
        assertNull(relation.getRelationTypeId());
    }

    @Test
    void shouldCreateArtistArtist_withRelationTypeId() {
        ArtistArtist relation = ArtistArtist.builder()
            .sourceArtistId(1L)
            .targetArtistId(2L)
            .relationTypeId(100L)
            .build();

        assertEquals(100L, relation.getRelationTypeId());
    }

    @Test
    void shouldFailToCreateArtistArtist_withNullSourceArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistArtist.builder().sourceArtistId(null));
    }

    @Test
    void shouldFailToCreateArtistArtist_withNullTargetArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistArtist.builder().targetArtistId(null));
    }

    // --- AlbumAlbum ---

    @Test
    void shouldCreateAlbumAlbum_whenAllMandatoryFieldsAreSet() {
        AlbumAlbum relation = AlbumAlbum.builder()
            .sourceAlbumId(1L)
            .targetAlbumId(2L)
            .build();

        assertNull(relation.getId());
        assertEquals(1L, relation.getSourceAlbumId());
        assertEquals(2L, relation.getTargetAlbumId());
        assertEquals(MasterEntityType.ALBUM, relation.getFirstEntityType());
        assertEquals(MasterEntityType.ALBUM, relation.getSecondEntityType());
        assertEquals(1L, relation.getFirstEntityId());
        assertEquals(2L, relation.getSecondEntityId());
        assertTrue(relation.supportsRelationTypes());
    }

    @Test
    void shouldFailToCreateAlbumAlbum_withNullSourceAlbumId() {
        assertThrows(NullPointerException.class, () -> AlbumAlbum.builder().sourceAlbumId(null));
    }

    @Test
    void shouldFailToCreateAlbumAlbum_withNullTargetAlbumId() {
        assertThrows(NullPointerException.class, () -> AlbumAlbum.builder().targetAlbumId(null));
    }

    // --- TrackTrack ---

    @Test
    void shouldCreateTrackTrack_whenAllMandatoryFieldsAreSet() {
        TrackTrack relation = TrackTrack.builder()
            .sourceTrackId(1L)
            .targetTrackId(2L)
            .build();

        assertNull(relation.getId());
        assertEquals(1L, relation.getSourceTrackId());
        assertEquals(2L, relation.getTargetTrackId());
        assertEquals(MasterEntityType.TRACK, relation.getFirstEntityType());
        assertEquals(MasterEntityType.TRACK, relation.getSecondEntityType());
        assertEquals(1L, relation.getFirstEntityId());
        assertEquals(2L, relation.getSecondEntityId());
        assertTrue(relation.supportsRelationTypes());
    }

    @Test
    void shouldFailToCreateTrackTrack_withNullSourceTrackId() {
        assertThrows(NullPointerException.class, () -> TrackTrack.builder().sourceTrackId(null));
    }

    @Test
    void shouldFailToCreateTrackTrack_withNullTargetTrackId() {
        assertThrows(NullPointerException.class, () -> TrackTrack.builder().targetTrackId(null));
    }
}
