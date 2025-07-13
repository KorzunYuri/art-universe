package yurykorzun.art.universe.music.data.approved.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.approved.controller.RelationController;
import yurykorzun.art.universe.music.data.approved.entity.DataSource;
import yurykorzun.art.universe.music.data.approved.entity.EntityType;
import yurykorzun.art.universe.music.data.approved.service.RelationService;

import static org.junit.jupiter.api.Assertions.*;

@WebMvcTest(controllers = RelationController.class)
class WebMvcConfigTest {

    @Autowired
    private ConversionService conversionService;
    
    @MockitoBean
    private RelationService relationService;
    
    @Test
    void conversionService_shouldConvertStringToEntityType() {
        // Given
        String artistString = "artist";
        String albumString = "album";
        String trackString = "track";
        String categoryString = "category";
        
        // When
        EntityType artistType = conversionService.convert(artistString, EntityType.class);
        EntityType albumType = conversionService.convert(albumString, EntityType.class);
        EntityType trackType = conversionService.convert(trackString, EntityType.class);
        EntityType categoryType = conversionService.convert(categoryString, EntityType.class);
        
        // Then
        assertEquals(EntityType.ARTIST, artistType);
        assertEquals(EntityType.ALBUM, albumType);
        assertEquals(EntityType.TRACK, trackType);
        assertEquals(EntityType.CATEGORY, categoryType);
    }
    
    @Test
    void conversionService_shouldConvertStringToEntityType_caseInsensitive() {
        // Given
        String artistString = "ARTIST";
        String albumString = "Album";
        
        // When
        EntityType artistType = conversionService.convert(artistString, EntityType.class);
        EntityType albumType = conversionService.convert(albumString, EntityType.class);
        
        // Then
        assertEquals(EntityType.ARTIST, artistType);
        assertEquals(EntityType.ALBUM, albumType);
    }
    
    @Test
    void conversionService_shouldConvertStringToDataSource() {
        // Given
        String lastfmString = "LASTFM";
        String spotifyString = "SPOTIFY";
        
        // When
        DataSource lastfmType = conversionService.convert(lastfmString, DataSource.class);
        DataSource spotifyType = conversionService.convert(spotifyString, DataSource.class);
        
        // Then
        assertEquals(DataSource.LASTFM, lastfmType);
        assertEquals(DataSource.SPOTIFY, spotifyType);
    }
    
    @Test
    void conversionService_shouldConvertStringToDataSource_caseInsensitive() {
        // Given
        String lastfmString = "lastfm";
        String spotifyString = "Spotify";
        
        // When
        DataSource lastfmType = conversionService.convert(lastfmString, DataSource.class);
        DataSource spotifyType = conversionService.convert(spotifyString, DataSource.class);
        
        // Then
        assertEquals(DataSource.LASTFM, lastfmType);
        assertEquals(DataSource.SPOTIFY, spotifyType);
    }
    
    @Test
    void conversionService_shouldThrowException_forInvalidEntityType() {
        // Given
        String invalidEntityType = "invalid";
        
        // When & Then
        ConversionFailedException exception = assertThrows(ConversionFailedException.class, () -> 
            conversionService.convert(invalidEntityType, EntityType.class));
        
        // Verify that the root cause is IllegalArgumentException
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Unknown entity type: invalid", exception.getCause().getMessage());
    }
    
    @Test
    void conversionService_shouldThrowException_forInvalidDataSource() {
        // Given
        String invalidDataSource = "invalid";
        
        // When & Then
        ConversionFailedException exception = assertThrows(ConversionFailedException.class, () -> 
            conversionService.convert(invalidDataSource, DataSource.class));
        
        // Verify that the root cause is IllegalArgumentException
        assertTrue(exception.getCause() instanceof IllegalArgumentException);
        assertEquals("Unknown data source: invalid", exception.getCause().getMessage());
    }
}
