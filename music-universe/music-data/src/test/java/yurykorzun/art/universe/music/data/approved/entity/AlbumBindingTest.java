package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumBindingTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidAlbumBinding() {
        Long referenceId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        AlbumBinding binding = AlbumBinding.builder()
            .referenceId(referenceId)
            .dataSource(dataSource)
            .externalId(externalId)
            .build();

        assertNull(binding.getId()); // ID is null before persistence
        assertEquals(referenceId, binding.getReferenceId());
        assertEquals(dataSource, binding.getDataSource());
        assertEquals(externalId, binding.getExternalId());
    }

    @Test
    void whenReferenceIdNotSet_shouldFailToCreateAlbumBinding() {
        assertThrows(NullPointerException.class, () -> 
            AlbumBinding.builder()
                .referenceId(null)
                .dataSource(DataSource.LASTFM)
                .externalId(123L)
                .build());
    }

    @Test
    void whenDataSourceNotSet_shouldFailToCreateAlbumBinding() {
        assertThrows(NullPointerException.class, () -> 
            AlbumBinding.builder()
                .referenceId(1L)
                .dataSource(null)
                .externalId(123L)
                .build());
    }

    @Test
    void whenExternalIdNotSet_shouldFailToCreateAlbumBinding() {
        assertThrows(NullPointerException.class, () -> 
            AlbumBinding.builder()
                .referenceId(1L)
                .dataSource(DataSource.LASTFM)
                .externalId(null)
                .build());
    }
}
