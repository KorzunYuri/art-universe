package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.CodedRegistry;

import static org.junit.jupiter.api.Assertions.*;

class EntityTypeTest {

    @Test
    void fromString_withValidName_shouldReturnCorrectEntityType() {
        // When
        EntityType artistType = EntityType.fromString("artist");
        EntityType albumType = EntityType.fromString("album");
        EntityType trackType = EntityType.fromString("track");
        EntityType categoryType = EntityType.fromString("category");
        
        // Then
        assertEquals(EntityType.ARTIST, artistType);
        assertEquals(EntityType.ALBUM, albumType);
        assertEquals(EntityType.TRACK, trackType);
        assertEquals(EntityType.CATEGORY, categoryType);
    }
    
    @Test
    void fromString_withCaseInsensitiveName_shouldReturnCorrectEntityType() {
        // When
        EntityType artistType = EntityType.fromString("ARTIST");
        EntityType albumType = EntityType.fromString("Album");
        EntityType trackType = EntityType.fromString("Track");
        EntityType categoryType = EntityType.fromString("CATEGORY");
        
        // Then
        assertEquals(EntityType.ARTIST, artistType);
        assertEquals(EntityType.ALBUM, albumType);
        assertEquals(EntityType.TRACK, trackType);
        assertEquals(EntityType.CATEGORY, categoryType);
    }
    
    @Test
    void fromString_withInvalidName_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            EntityType.fromString("invalid"));
    }
    
    @Test
    void getCode_shouldReturnCorrectCode() {
        // Then
        assertEquals(1, EntityType.ARTIST.getCode());
        assertEquals(2, EntityType.ALBUM.getCode());
        assertEquals(3, EntityType.TRACK.getCode());
        assertEquals(4, EntityType.CATEGORY.getCode());
    }
    
    @Test
    void getName_shouldReturnCorrectName() {
        // Then
        assertEquals("artist", EntityType.ARTIST.getName());
        assertEquals("album", EntityType.ALBUM.getName());
        assertEquals("track", EntityType.TRACK.getName());
        assertEquals("category", EntityType.CATEGORY.getName());
    }
    
    @Test
    void codedRegistry_shouldContainEntityTypes() {
        // When
        var artistType = CodedRegistry.getByCode(1, EntityType.class);
        var albumType = CodedRegistry.getByCode(2, EntityType.class);
        var trackType = CodedRegistry.getByCode(3, EntityType.class);
        var categoryType = CodedRegistry.getByCode(4, EntityType.class);
        
        // Then
        assertTrue(artistType.isPresent());
        assertTrue(albumType.isPresent());
        assertTrue(trackType.isPresent());
        assertTrue(categoryType.isPresent());
        
        assertEquals(EntityType.ARTIST, artistType.get());
        assertEquals(EntityType.ALBUM, albumType.get());
        assertEquals(EntityType.TRACK, trackType.get());
        assertEquals(EntityType.CATEGORY, categoryType.get());
    }
}
