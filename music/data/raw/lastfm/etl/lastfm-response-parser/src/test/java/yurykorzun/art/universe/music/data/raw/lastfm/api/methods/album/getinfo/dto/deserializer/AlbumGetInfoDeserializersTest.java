package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.deserializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.common.config.TestBeansConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class AlbumGetInfoDeserializersTest {

    private final ObjectMapper objectMapper = TestBeansConfig.getObjectMapper();

    @Test
    void shouldDeserializeAlbumWithEmptyTagsString() throws IOException {
        // Given
        Path jsonPath = Paths.get("src/test/resources/apiclient/responses/album.getInfo.tags-is-an-empty-string.json");
        String jsonContent = Files.readString(jsonPath);

        // When
        AlbumGetInfoDtoRoot result = objectMapper.readValue(jsonContent, AlbumGetInfoDtoRoot.class);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAlbum());
        assertEquals("Everything Is Peaceful Love", result.getAlbum().getName());
        assertEquals("Bon Iver", result.getAlbum().getArtistName());
        
        // Tags should be empty list, not null
        assertNotNull(result.getAlbum().getTags());
        assertNotNull(result.getAlbum().getTags().getTags());
        assertTrue(result.getAlbum().getTags().getTags().isEmpty());
        
        // Tracks should be parsed correctly
        assertNotNull(result.getAlbum().getTracksObject());
        assertNotNull(result.getAlbum().getTracksObject().getTracks());
        assertEquals(5, result.getAlbum().getTracksObject().getTracks().size());
        assertEquals("Everything Is Peaceful Love", result.getAlbum().getTracksObject().getTracks().get(0).getName());
    }

    @Test
    void shouldDeserializeAlbumWithSingleTrackAsObject() throws IOException {
        // Given
        Path jsonPath = Paths.get("src/test/resources/apiclient/responses/album.getinfo.track-is-an-object.json");
        String jsonContent = Files.readString(jsonPath);

        // When
        AlbumGetInfoDtoRoot result = objectMapper.readValue(jsonContent, AlbumGetInfoDtoRoot.class);

        // Then
        assertNotNull(result);
        assertNotNull(result.getAlbum());
        assertEquals("If I Ever Lose My Faith in You", result.getAlbum().getName());
        assertEquals("Disturbed", result.getAlbum().getArtistName());
        
        // Single track should be converted to list with one element
        AlbumGetInfoTracksDto tracksObject = result.getAlbum().getTracksObject();
        assertNotNull(tracksObject);
        List<AlbumGetInfoTrackDto> tracks = tracksObject.getTracks();
        assertNotNull(tracks);
        assertEquals(1, tracks.size());
        AlbumGetInfoTrackDto firstTrack = tracks.getFirst();
        assertEquals("If I Ever Lose My Faith in You", firstTrack.getName());
        assertEquals(274, firstTrack.getDuration());
        assertEquals(1, firstTrack.getAttr().getRank());
        
        // Tags should be parsed correctly
        AlbumGetInfoTagsDto tagsObject = result.getAlbum().getTags();
        assertNotNull(tagsObject);
        List<AlbumGetInfoTagDto> tags = tagsObject.getTags();
        assertNotNull(tags);
        assertEquals(1, tags.size());
        assertEquals("hard rock", tags.getFirst().getName());
    }
}
