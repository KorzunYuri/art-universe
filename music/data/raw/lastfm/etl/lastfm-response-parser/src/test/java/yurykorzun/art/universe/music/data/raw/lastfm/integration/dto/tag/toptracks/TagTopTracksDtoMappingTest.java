package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptracks;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.test.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TagTopTracksDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void parse_shouldParseCorrectly_whenTagTopTagsResponseProvided() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopTracks");

        // Parse JSON into DTO
        TagTopTracksDtoRoot dtoRoot = mapper.readValue(responseJsonString, TagTopTracksDtoRoot.class);
        assertNotNull(dtoRoot, "dtoRoot should not be null");
        assertNotNull(dtoRoot.getRootObject(), "Root 'tracks' object should not be null");

        // Check the number of tracks
        List<TagTopTracksTrackDto> tracks = dtoRoot.getRootObject().getTracks();
        assertNotNull(tracks, "Tracks list should not be null");
        assertEquals(50, tracks.size(), "The number of tracks should be 2");

        // Validate the first track
        TagTopTracksTrackDto firstTrack = tracks.get(0);
        assertEquals("Smells Like Teen Spirit", firstTrack.getName(), "Incorrect track name");
        assertEquals(301, firstTrack.getDuration(), "Incorrect track duration");
        assertNotNull(firstTrack.getArtist(), "Artist information should not be null");
        assertEquals("Nirvana", firstTrack.getArtist().getName(), "Incorrect artist name");
        assertNotNull(firstTrack.getRankInfo(), "Rank information should not be null");
        assertEquals(1, firstTrack.getRankInfo().getRank(), "Incorrect track rank");

        // Validate the second track
        TagTopTracksTrackDto secondTrack = tracks.get(1);
        assertEquals("Mr. Brightside", secondTrack.getName(), "Incorrect track name");
        assertEquals(224, secondTrack.getDuration(), "Incorrect track duration");
        assertNotNull(secondTrack.getArtist(), "Artist information should not be null");
        assertEquals("The Killers", secondTrack.getArtist().getName(), "Incorrect artist name");
        assertNotNull(secondTrack.getRankInfo(), "Rank information should not be null");
        assertEquals(2, secondTrack.getRankInfo().getRank(), "Incorrect track rank");
    }
}
