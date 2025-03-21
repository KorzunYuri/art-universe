package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.dto.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TagTopArtistsMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void givenTagTopTagsResponse_whenParsed_thenParsedCorrectly() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopArtists");
        TopArtistsDtoRoot root = mapper.readValue(responseJsonString, TopArtistsDtoRoot.class);

        assertNotNull(root);

        TopArtistsDto topArtists = root.getTopArtists();
        assertNotNull(topArtists);

        TopArtistsPageInfo pageInfo = topArtists.getPageInfo();
        assertNotNull(pageInfo);
        assertEquals("rock", pageInfo.getTag());
        assertEquals(1, pageInfo.getPageNumber());
        assertEquals(50, pageInfo.getRecordsPerPage());
        assertEquals(3578, pageInfo.getPagesTotal());
        assertEquals(178853, pageInfo.getRecordsTotal());

        List<ArtistsRankedDto> artists = topArtists.getArtists();
        assertNotNull(artists);
        assertEquals(50, artists.size());

        ArtistsRankedDto artist = artists.get(0);
        assertEquals("Coldplay", artist.getName());
        assertEquals("cc197bad-dc9c-440d-a5b5-d52ba2e14234", artist.getMbid());
        assertEquals("https://www.last.fm/music/Coldplay", artist.getUrl());

        ArtistRecordInfo recordInfo = artist.getRecordInfo();
        assertNotNull(recordInfo);
        assertEquals(1, recordInfo.getRank());
    }
}
