package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TrackBindingTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidTrackBinding() {
        Long referenceId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        TrackBinding binding = TrackBinding.builder()
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
    void whenReferenceIdNotSet_shouldFailToCreateTrackBinding() {
        assertThrows(NullPointerException.class, () -> TrackBinding.builder().referenceId(null));
    }

    @Test
    void whenDataSourceNotSet_shouldFailToCreateTrackBinding() {
        assertThrows(NullPointerException.class, () -> TrackBinding.builder().dataSource(null));
    }

    @Test
    void whenExternalIdNotSet_shouldFailToCreateTrackBinding() {
        assertThrows(NullPointerException.class, () -> TrackBinding.builder().externalId(null));
    }
}
