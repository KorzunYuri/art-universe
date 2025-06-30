package yurykorzun.art.universe.music.data.approved.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CategoryBindingTest {

    @Test
    void shouldCreateCategoryBinding_whenAllMandatoryFieldsAreSet() {
        // given
        Long referenceId = 1L;
        DataSource dataSource = DataSource.LASTFM;
        Long externalId = 123L;

        // when
        CategoryBinding binding = CategoryBinding.builder()
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
    void shouldFailToCreateCategoryBinding_withNullReferenceId() {
        assertThrows(NullPointerException.class, () -> CategoryBinding.builder().referenceId(null));
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
