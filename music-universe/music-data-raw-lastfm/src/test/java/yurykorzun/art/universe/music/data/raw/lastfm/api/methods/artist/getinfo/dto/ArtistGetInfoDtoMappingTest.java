package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.dto;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.dto.ArtistDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.StatsDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistGetInfoDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenArtistGetInfoResponseProvided() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getInfo");

        ArtistGetInfoDtoRoot root = mapper.readValue(responseJsonString, ArtistGetInfoDtoRoot.class);
        assertNotNull(root);

        //  check artist
        ArtistGetInfoArtistDto artist = root.getArtist();
        assertNotNull(artist);
        assertEquals("Cattle Decapitation", artist.getName());
        assertEquals("0bfb489b-7da8-4b28-b144-eee7599ed1e9", artist.getMbid());
        assertEquals("https://www.last.fm/music/Cattle+Decapitation", artist.getUrl());
        assertEquals(0, artist.getStreamable());
        assertEquals(1, artist.getOnTour());
        // check stats
        StatsDto stats = artist.getStats();
        assertNotNull(stats);
        assertEquals(193218, stats.getListeners());
        assertEquals(10828364, stats.getPlayCount());

        // check similar artists
        List<ArtistGetInfoSimilarArtistDto> similarArtists = artist.getSimilarArtistsObject().getArtists();
        assertNotNull(similarArtists);
        assertFalse(similarArtists.isEmpty());
        assertEquals(5, similarArtists.size());
        ArtistDto firstSimilar = similarArtists.get(0);
        assertEquals("Dying Fetus", firstSimilar.getName());
        assertEquals("https://www.last.fm/music/Dying+Fetus", firstSimilar.getUrl());

        // check tags
        ArtistGetInfoArtistTagsDto tagsObject = artist.getTagsObject();
        assertNotNull(tagsObject);
        List<ArtistGetInfoArtistTagDto> tags = tagsObject.getTags();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        assertEquals(5, tags.size());
        ArtistGetInfoArtistTagDto tag = tags.get(0);
        assertEquals("grindcore", tag.getName());
        assertEquals("https://www.last.fm/tag/grindcore", tag.getUrl());

    }
}
