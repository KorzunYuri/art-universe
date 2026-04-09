package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.CodedRegistry;

import static org.junit.jupiter.api.Assertions.*;

class EntityTypeTest {

    @Test
    void fromString_withValidName_shouldReturnCorrectEntityType() {
        // When
        MasterEntityType artistType = MasterEntityType.fromString("artist");
        MasterEntityType albumType = MasterEntityType.fromString("album");
        MasterEntityType trackType = MasterEntityType.fromString("track");
        MasterEntityType categoryType = MasterEntityType.fromString("category");
        
        // Then
        assertEquals(MasterEntityType.ARTIST, artistType);
        assertEquals(MasterEntityType.ALBUM, albumType);
        assertEquals(MasterEntityType.TRACK, trackType);
        assertEquals(MasterEntityType.CATEGORY, categoryType);
    }
    
    @Test
    void fromString_withCaseInsensitiveName_shouldReturnCorrectEntityType() {
        // When
        MasterEntityType artistType = MasterEntityType.fromString("ARTIST");
        MasterEntityType albumType = MasterEntityType.fromString("Album");
        MasterEntityType trackType = MasterEntityType.fromString("Track");
        MasterEntityType categoryType = MasterEntityType.fromString("CATEGORY");
        
        // Then
        assertEquals(MasterEntityType.ARTIST, artistType);
        assertEquals(MasterEntityType.ALBUM, albumType);
        assertEquals(MasterEntityType.TRACK, trackType);
        assertEquals(MasterEntityType.CATEGORY, categoryType);
    }
    
    @Test
    void fromString_withInvalidName_shouldThrowException() {
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            MasterEntityType.fromString("invalid"));
    }
    
    @Test
    void getCode_shouldReturnCorrectCode() {
        // Then
        assertEquals(1, MasterEntityType.ARTIST.getCode());
        assertEquals(2, MasterEntityType.ALBUM.getCode());
        assertEquals(3, MasterEntityType.TRACK.getCode());
        assertEquals(4, MasterEntityType.CATEGORY.getCode());
    }
    
    @Test
    void getName_shouldReturnCorrectName() {
        // Then
        assertEquals("artist", MasterEntityType.ARTIST.getName());
        assertEquals("album", MasterEntityType.ALBUM.getName());
        assertEquals("track", MasterEntityType.TRACK.getName());
        assertEquals("category", MasterEntityType.CATEGORY.getName());
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
