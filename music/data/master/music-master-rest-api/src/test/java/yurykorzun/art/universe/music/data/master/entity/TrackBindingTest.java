package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.master.model.DataSource;

import static org.junit.jupiter.api.Assertions.*;

public class TrackBindingTest {

    @Test
    void whenMandatoryFieldsAreSet_shouldCreateValidTrackBinding() {
        Long masterId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        TrackBinding binding = TrackBinding.builder()
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
    void whenMasterIdNotSet_shouldFailToCreateTrackBinding() {
        assertThrows(NullPointerException.class, () -> TrackBinding.builder().masterId(null));
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
