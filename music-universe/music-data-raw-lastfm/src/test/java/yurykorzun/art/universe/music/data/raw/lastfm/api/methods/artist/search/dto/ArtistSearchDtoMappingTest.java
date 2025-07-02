package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.search.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistSearchDtoMappingTest {

    private ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenArtistSearchResponseProvided() throws IOException {

        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.search");
        ArtistSearchDtoRoot dtoRoot = mapper.readValue(responseJsonString, ArtistSearchDtoRoot.class);

        assertNotNull(dtoRoot);

        ArtistSearchResultsDto rootObject = dtoRoot.getRootObject();
        assertNotNull(rootObject);

        ArtistSearchMatchesDto matches = rootObject.getMatches();
        assertNotNull(matches);

        List<ArtistSearchArtistDto> artists = matches.getArtists();
        assertNotNull(artists);
        assertEquals(30, artists.size());

        ArtistSearchArtistDto artist = artists.get(0);
        assertNotNull(artist);
        assertEquals("The Smashing Pumpkins", artist.getName());
        assertEquals("ba0d6274-db14-4ef5-b28d-657ebde1a396", artist.getMbid());
        assertEquals("https://www.last.fm/music/The+Smashing+Pumpkins", artist.getUrl());
        assertEquals(4259083, artist.getListenersCount());
        assertEquals(0, artist.getStreamable());
    }
}
