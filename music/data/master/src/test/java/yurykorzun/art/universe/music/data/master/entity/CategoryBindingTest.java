package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryBindingTest {

    @Test
    void shouldCreateCategoryBinding_whenAllMandatoryFieldsAreSet() {
        // given
        Long masterId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        // when
        CategoryBinding binding = CategoryBinding.builder()
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
    void shouldFailToCreateCategoryBinding_withNullMasterId() {
        assertThrows(NullPointerException.class, () -> CategoryBinding.builder().masterId(null));
    }

    @Test
    void shouldFailToCreateCategoryBinding_withNullDataSource() {
        assertThrows(NullPointerException.class, () -> CategoryBinding.builder().dataSource(null));
    }

    @Test
    void shouldFailToCreateCategoryBinding_withNullExternalId() {
        assertThrows(NullPointerException.class, () -> CategoryBinding.builder().externalId(null));
    }
}
