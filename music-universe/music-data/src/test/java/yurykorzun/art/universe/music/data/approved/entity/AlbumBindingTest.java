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
        assertThrows(NullPointerException.class, () -> AlbumBinding.builder().referenceId(null));
    }

    @Test
    void whenDataSourceNotSet_shouldFailToCreateAlbumBinding() {
        assertThrows(NullPointerException.class, () -> AlbumBinding.builder().dataSource(null));
    }

    @Test
    void whenExternalIdNotSet_shouldFailToCreateAlbumBinding() {
        assertThrows(NullPointerException.class, () -> AlbumBinding.builder().externalId(null));
    }
}
