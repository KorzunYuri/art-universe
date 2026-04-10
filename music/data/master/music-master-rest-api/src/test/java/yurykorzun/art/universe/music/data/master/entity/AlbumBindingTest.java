package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.model.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumBindingTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidAlbumBinding() {
        Long masterId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        AlbumBinding binding = AlbumBinding.builder()
            .masterId(masterId)
            .dataSource(dataSource)
            .externalId(externalId)
            .build();

        assertNull(binding.getId()); // ID is null before persistence
        assertEquals(masterId, binding.getMasterId());
        assertEquals(dataSource, binding.getDataSource());
        assertEquals(externalId, binding.getExternalId());
    }

    @Test
    void whenMasterIdNotSet_shouldFailToCreateAlbumBinding() {
        assertThrows(NullPointerException.class, () -> AlbumBinding.builder().masterId(null));
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
