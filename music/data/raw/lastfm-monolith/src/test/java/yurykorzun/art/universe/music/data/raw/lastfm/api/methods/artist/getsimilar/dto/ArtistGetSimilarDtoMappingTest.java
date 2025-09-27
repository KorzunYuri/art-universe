package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.dto;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ArtistGetSimilarDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenArtistGetSimilarDtoProvided() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getSimilar");
        ArtistGetSimilarDtoRoot dtoRoot = mapper.readValue(responseJsonString, ArtistGetSimilarDtoRoot.class);

        assertNotNull(dtoRoot);

        ArtistGetSimilarSimilarArtistsDto rootObject = dtoRoot.getRootObject();
        assertNotNull(rootObject);

        List<ArtistGetSimilarArtistDto> artists = rootObject.getArtists();
        assertNotNull(artists);
        assertEquals(50, artists.size());

        ArtistGetSimilarArtistDto artist = artists.get(1); // second artist contains fractional match coefficient
        assertNotNull(artist);
        assertEquals("Kylie Minogue", artist.getName());
        assertEquals("2fddb92d-24b2-46a5-bf28-3aed46f4684c", artist.getMbid());
        assertEquals("https://www.last.fm/music/Kylie+Minogue", artist.getUrl());
        assertEquals(0.636364, artist.getMatchCoeff(), 0.00001f);
        assertEquals(0, artist.getStreamable());
    }

}
