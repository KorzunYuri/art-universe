package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.model.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistBindingTest {

    @Test
    void shouldCreateArtistBinding_whenAllMandatoryFieldsAreSet() {
        // given
        Long masterId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        // when
        ArtistBinding binding = ArtistBinding.builder()
            .masterId(masterId)
            .dataSource(dataSource)
            .externalId(externalId)
            .build();

        // then
        assertNull(binding.getId()); // Hibernate sets it on persist
        assertEquals(masterId, binding.getMasterId());
        assertEquals(dataSource, binding.getDataSource());
        assertEquals(externalId, binding.getExternalId());
    }

    @Test
    void shouldFailToCreateArtistBinding_withNullMasterId() {
        assertThrows(NullPointerException.class, () -> ArtistBinding.builder().masterId(null));
    }

    @Test
    void shouldFailToCreateArtistBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> ArtistBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateArtistBinding_withNullExternalId() {
        assertThrows(NullPointerException.class, () -> ArtistBinding.builder().externalId(null));
    }
}
