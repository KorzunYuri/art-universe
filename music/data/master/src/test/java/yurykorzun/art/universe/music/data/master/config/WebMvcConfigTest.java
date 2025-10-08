package yurykorzun.art.universe.music.data.master.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.core.convert.ConversionService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import yurykorzun.art.universe.music.data.master.controller.RelationController;
import yurykorzun.art.universe.music.data.master.entity.DataSource;
import yurykorzun.art.universe.music.data.master.entity.MasterEntityType;
import yurykorzun.art.universe.music.data.master.service.RelationService;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contains tests of auto-conversion of data type in controllers.
 */
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
        MasterEntityType artistType = conversionService.convert(artistString, MasterEntityType.class);
        MasterEntityType albumType = conversionService.convert(albumString, MasterEntityType.class);
        MasterEntityType trackType = conversionService.convert(trackString, MasterEntityType.class);
        MasterEntityType categoryType = conversionService.convert(categoryString, MasterEntityType.class);
        
        // Then
        assertEquals(MasterEntityType.ARTIST, artistType);
        assertEquals(MasterEntityType.ALBUM, albumType);
        assertEquals(MasterEntityType.TRACK, trackType);
        assertEquals(MasterEntityType.CATEGORY, categoryType);
    }
    
    @Test
    void conversionService_shouldConvertStringToEntityType_caseInsensitive() {
        // Given
        String artistString = "ARTIST";
        String albumString = "Album";
        
        // When
        MasterEntityType artistType = conversionService.convert(artistString, MasterEntityType.class);
        MasterEntityType albumType = conversionService.convert(albumString, MasterEntityType.class);
        
        // Then
        assertEquals(MasterEntityType.ARTIST, artistType);
        assertEquals(MasterEntityType.ALBUM, albumType);
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
            conversionService.convert(invalidEntityType, MasterEntityType.class));
        
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
