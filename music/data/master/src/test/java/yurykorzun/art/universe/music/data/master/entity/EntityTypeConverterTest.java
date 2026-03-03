package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.common.persistence.converter.EntityTypeConverter;

import static org.junit.jupiter.api.Assertions.*;

class EntityTypeConverterTest {

    private EntityTypeConverter converter;
    
    @BeforeEach
    void setUp() {
        converter = new EntityTypeConverter();
    }
    
    @Test
    void convertToDatabaseColumn_shouldReturnCode() {
        // Given
        MasterEntityType entityType = MasterEntityType.ARTIST;
        
        // When
        Integer code = converter.convertToDatabaseColumn(entityType);
        
        // Then
        assertEquals(entityType.getCode(), code);
    }
    
    @Test
    void convertToDatabaseColumn_withNull_shouldReturnNull() {
        // When
        Integer code = converter.convertToDatabaseColumn(null);
        
        // Then
        assertNull(code);
    }
    
    @Test
    void convertToEntityAttribute_shouldReturnEntityType() {
        // Given
        Integer code = MasterEntityType.ARTIST.getCode();
        
        // When
        MasterEntityType entityType = converter.convertToEntityAttribute(code);
        
        // Then
        assertEquals(MasterEntityType.ARTIST, entityType);
    }
    
    @Test
    void convertToEntityAttribute_withNull_shouldReturnNull() {
        // When
        MasterEntityType entityType = converter.convertToEntityAttribute(null);
        
        // Then
        assertNull(entityType);
    }
    
    @Test
    void convertToEntityAttribute_withInvalidCode_shouldThrowException() {
        // Given
        Integer invalidCode = 999;
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            converter.convertToEntityAttribute(invalidCode));
    }
    
    @Test
    void codedRegistry_shouldContainEntityTypes() {
        // When
        var artistType = CodedRegistry.getByCode(1, MasterEntityType.class);
        var albumType = CodedRegistry.getByCode(2, MasterEntityType.class);
        var trackType = CodedRegistry.getByCode(3, MasterEntityType.class);
        var categoryType = CodedRegistry.getByCode(4, MasterEntityType.class);
        
        // Then
        assertTrue(artistType.isPresent());
        assertTrue(albumType.isPresent());
        assertTrue(trackType.isPresent());
        assertTrue(categoryType.isPresent());
        
        assertEquals(MasterEntityType.ARTIST, artistType.get());
        assertEquals(MasterEntityType.ALBUM, albumType.get());
        assertEquals(MasterEntityType.TRACK, trackType.get());
        assertEquals(MasterEntityType.CATEGORY, categoryType.get());
    }
}
