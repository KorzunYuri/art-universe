package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Utility class for processor tests with common verification methods
 */
@Component
public class LastfmApiResponseProcessorTestHelper {

    private final LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    public LastfmApiResponseProcessorTestHelper(
        LastfmAttributeHistoryRecordRepository attributeHistoryRepository
    ) {
        this.attributeHistoryRepository = attributeHistoryRepository;
    }

    /**
     * Verify string attribute value in history records (non-scoped)
     * 
     * @param entity The entity to check attributes for
     * @param attribute The attribute to verify
     * @param expectedValue The expected string value
     */
    public void verifyStringAttribute(BaseLastfmEntity entity, LastfmAttribute attribute, String expectedValue) {
        verifyStringAttribute(entity.getType(), entity.getId(), attribute, expectedValue);
    }

    /**
     * Verify string attribute value in history records (non-scoped)
     * 
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param attribute The attribute to verify
     * @param expectedValue The expected string value
     */
    public void verifyStringAttribute(LastfmEntityType entityType, Long entityId, LastfmAttribute attribute, String expectedValue) {
        List<LastfmAttributeHistoryRecord> records = attributeHistoryRepository.findAttributeValuesForEntity(
            attribute, entityType, entityId);
        
        assertFalse(records.isEmpty(), "Should have attribute history record for " + attribute.name());
        assertEquals(expectedValue, records.get(0).getStringValue(), 
            attribute.name() + " attribute value should match");
    }

    /**
     * Verify integer attribute value in history records (non-scoped)
     * 
     * @param entity The entity to check attributes for
     * @param attribute The attribute to verify
     * @param expectedValue The expected integer value
     */
    public void verifyNumericAttribute(BaseLastfmEntity entity, LastfmAttribute attribute, long expectedValue) {
        verifyNumericAttribute(entity.getType(), entity.getId(), attribute, expectedValue);
    }

    /**
     * Verify integer attribute value in history records (non-scoped)
     * 
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param attribute The attribute to verify
     * @param expectedValue The expected integer value
     */
    public void verifyNumericAttribute(LastfmEntityType entityType, Long entityId, LastfmAttribute attribute, long expectedValue) {
        List<LastfmAttributeHistoryRecord> records = attributeHistoryRepository.findAttributeValuesForEntity(
            attribute, entityType, entityId);
        
        assertFalse(records.isEmpty(), "Should have attribute history record for " + attribute.name());
        assertEquals(expectedValue, records.get(0).getNumericValue(),
            attribute.name() + " attribute value should match");
    }

    /**
     * Verify boolean attribute value in history records (stored as int 0/1) (non-scoped)
     * 
     * @param entity The entity to check attributes for
     * @param attribute The attribute to verify
     * @param expectedValue The expected boolean value
     */
    public void verifyBooleanAttribute(BaseLastfmEntity entity, LastfmAttribute attribute, boolean expectedValue) {
        verifyNumericAttribute(entity, attribute, expectedValue ? 1 : 0);
    }
    
    /**
     * Verify string attribute value in history records with scope
     * 
     * @param entity The entity to check attributes for
     * @param scopeEntity The scope entity
     * @param attribute The attribute to verify
     * @param expectedValue The expected string value
     */
    public void verifyStringAttributeWithScope(BaseLastfmEntity entity, BaseLastfmEntity scopeEntity, 
                                              LastfmAttribute attribute, String expectedValue) {
        List<LastfmAttributeHistoryRecord> records = attributeHistoryRepository.findAttributeValuesForEntityWithScope(
            attribute, entity.getType(), entity.getId(), scopeEntity.getType(), scopeEntity.getId());
        
        assertFalse(records.isEmpty(), 
            "Should have scoped attribute history record for " + attribute.name());
        assertEquals(expectedValue, records.get(0).getStringValue(), 
            attribute.name() + " scoped attribute value should match");
    }
    
    /**
     * Verify integer attribute value in history records with scope
     * 
     * @param entity The entity to check attributes for
     * @param scopeEntity The scope entity
     * @param attribute The attribute to verify
     * @param expectedValue The expected integer value
     */
    public void verifyIntAttributeWithScope(BaseLastfmEntity entity, BaseLastfmEntity scopeEntity, 
                                           LastfmAttribute attribute, int expectedValue) {
        List<LastfmAttributeHistoryRecord> records = attributeHistoryRepository.findAttributeValuesForEntityWithScope(
            attribute, entity.getType(), entity.getId(), scopeEntity.getType(), scopeEntity.getId());
        
        assertFalse(records.isEmpty(), 
            "Should have scoped attribute history record for " + attribute.name());
        assertEquals(expectedValue, records.get(0).getNumericValue(),
            attribute.name() + " scoped attribute value should match");
    }
    
    /**
     * Verify boolean attribute value in history records with scope (stored as int 0/1)
     * 
     * @param entity The entity to check attributes for
     * @param scopeEntity The scope entity
     * @param attribute The attribute to verify
     * @param expectedValue The expected boolean value
     */
    public void verifyBooleanAttributeWithScope(BaseLastfmEntity entity, BaseLastfmEntity scopeEntity, 
                                               LastfmAttribute attribute, boolean expectedValue) {
        verifyIntAttributeWithScope(entity, scopeEntity, attribute, expectedValue ? 1 : 0);
    }
}
