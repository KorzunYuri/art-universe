package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RelationBindingEntityTest {

    // --- ArtistAlbumBinding ---

    @Test
    void shouldCreateArtistAlbumBinding_whenAllMandatoryFieldsAreSet() {
        ArtistAlbumBinding binding = ArtistAlbumBinding.builder()
            .masterBindingId(1L)
            .dataSource(DataSource.LASTFM)
            .externalArtistId(10L)
            .externalAlbumId(20L)
            .build();

        assertNull(binding.getId());
        assertEquals(1L, binding.getMasterBindingId());
        assertEquals(DataSource.LASTFM, binding.getDataSource());
        assertEquals(10L, binding.getExternalArtistId());
        assertEquals(20L, binding.getExternalAlbumId());
        assertEquals(MasterEntityType.ARTIST, binding.getFirstEntityType());
        assertEquals(MasterEntityType.ALBUM, binding.getSecondEntityType());
        assertEquals(10L, binding.getExternalFirstEntityId());
        assertEquals(20L, binding.getExternalSecondEntityId());
    }

    @Test
    void shouldFailToCreateArtistAlbumBinding_withNullMasterBindingId() {
        assertThrows(NullPointerException.class, () -> ArtistAlbumBinding.builder().masterBindingId(null));
    }

    @Test
    void shouldFailToCreateArtistAlbumBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> ArtistAlbumBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateArtistAlbumBinding_withNullExternalArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistAlbumBinding.builder().externalArtistId(null));
    }

    @Test
    void shouldFailToCreateArtistAlbumBinding_withNullExternalAlbumId() {
        assertThrows(NullPointerException.class, () -> ArtistAlbumBinding.builder().externalAlbumId(null));
    }

    // --- ArtistTrackBinding ---

    @Test
    void shouldCreateArtistTrackBinding_whenAllMandatoryFieldsAreSet() {
        ArtistTrackBinding binding = ArtistTrackBinding.builder()
            .masterBindingId(1L)
            .dataSource(DataSource.SPOTIFY)
            .externalArtistId(10L)
            .externalTrackId(30L)
            .build();

        assertNull(binding.getId());
        assertEquals(1L, binding.getMasterBindingId());
        assertEquals(DataSource.SPOTIFY, binding.getDataSource());
        assertEquals(MasterEntityType.ARTIST, binding.getFirstEntityType());
        assertEquals(MasterEntityType.TRACK, binding.getSecondEntityType());
        assertEquals(10L, binding.getExternalFirstEntityId());
        assertEquals(30L, binding.getExternalSecondEntityId());
    }

    @Test
    void shouldFailToCreateArtistTrackBinding_withNullMasterBindingId() {
        assertThrows(NullPointerException.class, () -> ArtistTrackBinding.builder().masterBindingId(null));
    }

    @Test
    void shouldFailToCreateArtistTrackBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> ArtistTrackBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateArtistTrackBinding_withNullExternalArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistTrackBinding.builder().externalArtistId(null));
    }

    @Test
    void shouldFailToCreateArtistTrackBinding_withNullExternalTrackId() {
        assertThrows(NullPointerException.class, () -> ArtistTrackBinding.builder().externalTrackId(null));
    }

    // --- AlbumTrackBinding ---

    @Test
    void shouldCreateAlbumTrackBinding_whenAllMandatoryFieldsAreSet() {
        AlbumTrackBinding binding = AlbumTrackBinding.builder()
            .masterBindingId(1L)
            .dataSource(DataSource.MUSICBRAINZ)
            .externalAlbumId(20L)
            .externalTrackId(30L)
            .build();

        assertNull(binding.getId());
        assertEquals(1L, binding.getMasterBindingId());
        assertEquals(DataSource.MUSICBRAINZ, binding.getDataSource());
        assertEquals(MasterEntityType.ALBUM, binding.getFirstEntityType());
        assertEquals(MasterEntityType.TRACK, binding.getSecondEntityType());
        assertEquals(20L, binding.getExternalFirstEntityId());
        assertEquals(30L, binding.getExternalSecondEntityId());
    }

    @Test
    void shouldFailToCreateAlbumTrackBinding_withNullMasterBindingId() {
        assertThrows(NullPointerException.class, () -> AlbumTrackBinding.builder().masterBindingId(null));
    }

    @Test
    void shouldFailToCreateAlbumTrackBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> AlbumTrackBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateAlbumTrackBinding_withNullExternalAlbumId() {
        assertThrows(NullPointerException.class, () -> AlbumTrackBinding.builder().externalAlbumId(null));
    }

    @Test
    void shouldFailToCreateAlbumTrackBinding_withNullExternalTrackId() {
        assertThrows(NullPointerException.class, () -> AlbumTrackBinding.builder().externalTrackId(null));
    }

    // --- ArtistCategoryBinding ---

    @Test
    void shouldCreateArtistCategoryBinding_whenAllMandatoryFieldsAreSet() {
        ArtistCategoryBinding binding = ArtistCategoryBinding.builder()
            .masterBindingId(1L)
            .dataSource(DataSource.LASTFM)
            .externalArtistId(10L)
            .externalCategoryId(40L)
            .build();

        assertNull(binding.getId());
        assertEquals(1L, binding.getMasterBindingId());
        assertEquals(DataSource.LASTFM, binding.getDataSource());
        assertEquals(MasterEntityType.ARTIST, binding.getFirstEntityType());
        assertEquals(MasterEntityType.CATEGORY, binding.getSecondEntityType());
        assertEquals(10L, binding.getExternalFirstEntityId());
        assertEquals(40L, binding.getExternalSecondEntityId());
    }

    @Test
    void shouldFailToCreateArtistCategoryBinding_withNullMasterBindingId() {
        assertThrows(NullPointerException.class, () -> ArtistCategoryBinding.builder().masterBindingId(null));
    }

    @Test
    void shouldFailToCreateArtistCategoryBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> ArtistCategoryBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateArtistCategoryBinding_withNullExternalArtistId() {
        assertThrows(NullPointerException.class, () -> ArtistCategoryBinding.builder().externalArtistId(null));
    }

    @Test
    void shouldFailToCreateArtistCategoryBinding_withNullExternalCategoryId() {
        assertThrows(NullPointerException.class, () -> ArtistCategoryBinding.builder().externalCategoryId(null));
    }

    // --- AlbumCategoryBinding ---

    @Test
    void shouldCreateAlbumCategoryBinding_whenAllMandatoryFieldsAreSet() {
        AlbumCategoryBinding binding = AlbumCategoryBinding.builder()
            .masterBindingId(1L)
            .dataSource(DataSource.SPOTIFY)
            .externalAlbumId(20L)
            .externalCategoryId(40L)
            .build();

        assertNull(binding.getId());
        assertEquals(1L, binding.getMasterBindingId());
        assertEquals(DataSource.SPOTIFY, binding.getDataSource());
        assertEquals(MasterEntityType.ALBUM, binding.getFirstEntityType());
        assertEquals(MasterEntityType.CATEGORY, binding.getSecondEntityType());
        assertEquals(20L, binding.getExternalFirstEntityId());
        assertEquals(40L, binding.getExternalSecondEntityId());
    }

    @Test
    void shouldFailToCreateAlbumCategoryBinding_withNullMasterBindingId() {
        assertThrows(NullPointerException.class, () -> AlbumCategoryBinding.builder().masterBindingId(null));
    }

    @Test
    void shouldFailToCreateAlbumCategoryBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> AlbumCategoryBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateAlbumCategoryBinding_withNullExternalAlbumId() {
        assertThrows(NullPointerException.class, () -> AlbumCategoryBinding.builder().externalAlbumId(null));
    }

    @Test
    void shouldFailToCreateAlbumCategoryBinding_withNullExternalCategoryId() {
        assertThrows(NullPointerException.class, () -> AlbumCategoryBinding.builder().externalCategoryId(null));
    }

    // --- TrackCategoryBinding ---

    @Test
    void shouldCreateTrackCategoryBinding_whenAllMandatoryFieldsAreSet() {
        TrackCategoryBinding binding = TrackCategoryBinding.builder()
            .masterBindingId(1L)
            .dataSource(DataSource.MUSICBRAINZ)
            .externalTrackId(30L)
            .externalCategoryId(40L)
            .build();

        assertNull(binding.getId());
        assertEquals(1L, binding.getMasterBindingId());
        assertEquals(DataSource.MUSICBRAINZ, binding.getDataSource());
        assertEquals(MasterEntityType.TRACK, binding.getFirstEntityType());
        assertEquals(MasterEntityType.CATEGORY, binding.getSecondEntityType());
        assertEquals(30L, binding.getExternalFirstEntityId());
        assertEquals(40L, binding.getExternalSecondEntityId());
    }

    @Test
    void shouldFailToCreateTrackCategoryBinding_withNullMasterBindingId() {
        assertThrows(NullPointerException.class, () -> TrackCategoryBinding.builder().masterBindingId(null));
    }

    @Test
    void shouldFailToCreateTrackCategoryBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> TrackCategoryBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateTrackCategoryBinding_withNullExternalTrackId() {
        assertThrows(NullPointerException.class, () -> TrackCategoryBinding.builder().externalTrackId(null));
    }

    @Test
    void shouldFailToCreateTrackCategoryBinding_withNullExternalCategoryId() {
        assertThrows(NullPointerException.class, () -> TrackCategoryBinding.builder().externalCategoryId(null));
    }
}
