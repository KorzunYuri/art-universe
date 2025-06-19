package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistBindingTest {

    @Test
    void shouldCreateArtistBinding_whenAllMandatoryFieldsAreSet() {
        // given
        Long referenceId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        // when
        ArtistBinding binding = ArtistBinding.builder()
            .referenceId(referenceId)
            .dataSource(dataSource)
            .externalId(externalId)
            .build();

        // then
        assertNull(binding.getId()); // Hibernate sets it on persist
        assertEquals(referenceId, binding.getReferenceId());
        assertEquals(dataSource, binding.getDataSource());
        assertEquals(externalId, binding.getExternalId());
    }

    @Test
    void shouldFailToCreateArtistBinding_withNullReferenceId() {
        assertThrows(NullPointerException.class, () -> ArtistBinding.builder().referenceId(null));
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
