package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.attributes;

import lombok.Getter;
import lombok.Setter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.EntityDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing.mapping.EntityMapping;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EntityAttributeHandlerConstantTest {

    // Test DTO class
    @Getter
    @Setter
    static class TestDto implements EntityDto<TestEntity> {
        private final String url;
        private final Long usageCount;
        private Long matchCoeff;
        
        public TestDto(String url, Long usageCount, Long matchCoeff) {
            this.url = url;
            this.usageCount = usageCount;
            this.matchCoeff = matchCoeff;
        }

        @Override
        public String getName() {
            return "testDto";
        }

        @Override
        public LastfmEntityType getEntityType() {
            return LastfmEntityType.ARTIST;
        }

    }
    
    // Test Entity class
    @Getter
    @Setter
    static class TestEntity extends BaseLastfmEntity {
        private String url;
        private Long usageCount;
        private Long matchCoeff;
        
        public TestEntity(String url, Long usageCount, Long matchCoeff) {
            this.url = url;
            this.usageCount = usageCount;
            this.matchCoeff = matchCoeff;
        }

        @Override
        public long getId() {
            return 0;
        }

        @Override
        public LastfmEntityType getType() {
            return LastfmEntityType.ARTIST;
        }

        @Override
        public String getUniqueKey() {
            return "";
        }
    }

    @Test
    void shouldCreateNewRecord_shouldReturnFalse_whenAttributeIsConstant() {
        // Given
        EntityAttributeHandler<TestEntity, String, TestDto> handler = 
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.URL, // CONSTANT attribute
                false,
                TestEntity::getUrl,
                TestEntity::setUrl,
                TestDto::getUrl
            );
        
        TestEntity oldEntity = new TestEntity("old-url", 100L, 100L);
        TestDto dto = new TestDto("new-url", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);
        
        // When
        boolean shouldCreate = handler.shouldCreateNewRecord(mapping);
        
        // Then
        assertFalse(shouldCreate, "CONSTANT attributes should never create history records");
    }

    @Test
    void hasAttributeChanged_shouldReturnTrue_whenOldValueIsNull() {
        // Given
        EntityAttributeHandler<TestEntity, String, TestDto> handler = 
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.URL, // CONSTANT attribute
                false,
                TestEntity::getUrl,
                TestEntity::setUrl,
                TestDto::getUrl
            );
        
        TestEntity oldEntity = new TestEntity(null, 100L, 100L); // null URL
        TestDto dto = new TestDto("new-url", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);
        
        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);
        
        // Then
        assertTrue(hasChanged, "Should update CONSTANT attribute when old value is null");
    }

    @Test
    void hasAttributeChanged_shouldReturnTrue_whenOldValueIsEmpty() {
        // Given
        EntityAttributeHandler<TestEntity, String, TestDto> handler = 
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.URL, // CONSTANT attribute
                false,
                TestEntity::getUrl,
                TestEntity::setUrl,
                TestDto::getUrl
            );
        
        TestEntity oldEntity = new TestEntity("", 100L, 100L); // empty URL
        TestDto dto = new TestDto("new-url", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);
        
        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);
        
        // Then
        assertTrue(hasChanged, "Should update CONSTANT attribute when old value is empty");
    }

    @Test
    void hasAttributeChanged_shouldReturnTrue_whenOldValueIsWhitespace() {
        // Given
        EntityAttributeHandler<TestEntity, String, TestDto> handler = 
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.URL, // CONSTANT attribute
                false,
                TestEntity::getUrl,
                TestEntity::setUrl,
                TestDto::getUrl
            );
        
        TestEntity oldEntity = new TestEntity("   ", 100L, 100L); // whitespace URL
        TestDto dto = new TestDto("new-url", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);
        
        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);
        
        // Then
        assertTrue(hasChanged, "Should update CONSTANT attribute when old value is whitespace");
    }

    @Test
    void hasAttributeChanged_shouldReturnFalse_whenOldValueExists() {
        // Given
        EntityAttributeHandler<TestEntity, String, TestDto> handler = 
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.URL, // CONSTANT attribute
                false,
                TestEntity::getUrl,
                TestEntity::setUrl,
                TestDto::getUrl
            );
        
        TestEntity oldEntity = new TestEntity("existing-url", 100L, 100L); // existing URL
        TestDto dto = new TestDto("different-url", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);
        
        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);
        
        // Then
        assertFalse(hasChanged, "Should NOT update CONSTANT attribute when old value exists");
    }

    @Test
    void hasAttributeChanged_shouldReturnTrue_whenEntityIsNew() {
        // Given
        EntityAttributeHandler<TestEntity, String, TestDto> handler =
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.URL, // CONSTANT attribute
                false,
                TestEntity::getUrl,
                TestEntity::setUrl,
                TestDto::getUrl
            );
        
        TestDto dto = new TestDto("new-url", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(null); // new entity
        
        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);
        
        // Then
        assertTrue(hasChanged, "Should always set CONSTANT attribute for new entities");
    }

    @Test
    void hasAttributeChanged_shouldUseScd2Logic_whenAttributeIsSCD2() {
        // Given
        EntityAttributeHandler<TestEntity, Long, TestDto> handler =
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.MATCH_COEFF, // SCD2 attribute
                false,
                TestEntity::getMatchCoeff,
                TestEntity::setMatchCoeff,
                TestDto::getMatchCoeff
            );
        
        TestEntity oldEntity = new TestEntity("old-value", 100L, 100L);
        TestDto dto = new TestDto("new-value",100L, 10L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);
        
        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);
        
        // Then
        assertTrue(hasChanged, "Changed value of SCD2 attribute should trigger entity update");
    }
    
    @Test
    void hasAttributeChanged_shouldReturnFalse_whenIncreasingAttributeHasDecreased() {
        // Given
        EntityAttributeHandler<TestEntity, Long, TestDto> handler =
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.USAGE_COUNT, // increasing attribute
                false,
                TestEntity::getUsageCount,
                TestEntity::setUsageCount,
                TestDto::getUsageCount
            );

        TestEntity oldEntity = new TestEntity("old-value", 100L, 100L);
        TestDto dto = new TestDto("new-value",10L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);

        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);

        // Then
        assertFalse(hasChanged, "Decreased value of INCREASING attribute should not trigger entity update");
    }

    @Test
    void hasAttributeChanged_shouldReturnTrue_whenIncreasingAttributeHasIncreased() {
        // Given
        EntityAttributeHandler<TestEntity, Long, TestDto> handler =
            DefaultEntityAttributeHandler.forEmbeddedAttribute(
                LastfmAttribute.USAGE_COUNT, // increasing attribute
                false,
                TestEntity::getUsageCount,
                TestEntity::setUsageCount,
                TestDto::getUsageCount
            );

        TestEntity oldEntity = new TestEntity("old-value", 10L, 10L);
        TestDto dto = new TestDto("new-value", 100L, 100L);
        EntityMapping<TestEntity, TestDto> mapping = new EntityMapping<>(dto);
        mapping.setOldEntity(oldEntity);

        // When
        boolean hasChanged = handler.hasAttributeChanged(mapping);

        // Then
        assertTrue(hasChanged, "Increased value of INCREASING attribute should trigger entity update");
    }
}
