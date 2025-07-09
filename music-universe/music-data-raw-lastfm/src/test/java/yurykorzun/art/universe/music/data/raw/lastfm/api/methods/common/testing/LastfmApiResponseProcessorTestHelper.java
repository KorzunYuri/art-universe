package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.testing;

import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

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
     * Verify string attribute value in history records
     * 
     * @param entity The entity to check attributes for
     * @param attribute The attribute to verify
     * @param expectedValue The expected string value
     */
    public void verifyStringAttribute(BaseLastfmEntity entity, LastfmAttribute attribute, String expectedValue) {
        verifyStringAttribute(entity.getType(), entity.getId(), attribute, expectedValue);
    }

    /**
     * Verify string attribute value in history records
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
     * Verify integer attribute value in history records
     * 
     * @param entity The entity to check attributes for
     * @param attribute The attribute to verify
     * @param expectedValue The expected integer value
     */
    public void verifyIntAttribute(BaseLastfmEntity entity, LastfmAttribute attribute, int expectedValue) {
        verifyIntAttribute(entity.getType(), entity.getId(), attribute, expectedValue);
    }

    /**
     * Verify integer attribute value in history records
     * 
     * @param entityType The entity type
     * @param entityId The entity ID
     * @param attribute The attribute to verify
     * @param expectedValue The expected integer value
     */
    public void verifyIntAttribute(LastfmEntityType entityType, Long entityId, LastfmAttribute attribute, int expectedValue) {
        List<LastfmAttributeHistoryRecord> records = attributeHistoryRepository.findAttributeValuesForEntity(
            attribute, entityType, entityId);
        
        assertFalse(records.isEmpty(), "Should have attribute history record for " + attribute.name());
        assertEquals(expectedValue, records.get(0).getIntValue(), 
            attribute.name() + " attribute value should match");
    }

    /**
     * Verify boolean attribute value in history records (stored as int 0/1)
     * 
     * @param entity The entity to check attributes for
     * @param attribute The attribute to verify
     * @param expectedValue The expected boolean value
     */
    public void verifyBooleanAttribute(BaseLastfmEntity entity, LastfmAttribute attribute, boolean expectedValue) {
        verifyIntAttribute(entity, attribute, expectedValue ? 1 : 0);
    }

    /**
     * Verify that all attribute history records exist for an artist
     * 
     * @param artist The artist to check
     * @param attributes The attributes that should exist
     */
    public void verifyArtistAttributesExist(LastfmArtist artist, LastfmAttribute... attributes) {
        for (LastfmAttribute attribute : attributes) {
            List<LastfmAttributeHistoryRecord> records = attributeHistoryRepository.findAttributeValuesForEntity(
                attribute, artist.getType(), artist.getId());
            assertFalse(records.isEmpty(), 
                "Should have attribute history record for " + attribute.name());
        }
    }
}
