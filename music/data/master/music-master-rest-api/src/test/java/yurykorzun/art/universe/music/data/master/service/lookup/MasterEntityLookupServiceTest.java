package yurykorzun.art.universe.music.data.master.service.lookup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for MasterEntityLookupService focusing on module-specific functionality.
 * Basic lookup logic is tested in art-universe-commons BaseLookupServiceTest.
 */
@ExtendWith(MockitoExtension.class)
class MasterEntityLookupServiceTest {

    @Mock
    private EntityManager entityManager;

    @Test
    void createEntityMetadata_shouldReturnCorrectMetadataForEntityType() {
        // Given
        MasterEntityLookupService lookupService = new MasterEntityLookupService(entityManager, MasterEntityType.ARTIST);
        
        // When
        var metadata = lookupService.createEntityMetadata();

        // Then
        assertNotNull(metadata);
        assertEquals("artist", metadata.getTableName());
        assertEquals("artist_binding", metadata.getBindingTableName());
        assertEquals("artist_id", metadata.getIdFieldName());
        assertEquals("external_artist_id", metadata.getExternalIdFieldName());
    }

    @Test
    void getEntityType_shouldReturnCorrectEntityType() {
        // Given
        MasterEntityLookupService lookupService = new MasterEntityLookupService(entityManager, MasterEntityType.CATEGORY);
        
        // When & Then
        assertEquals(MasterEntityType.CATEGORY, lookupService.getEntityType());
    }
}
