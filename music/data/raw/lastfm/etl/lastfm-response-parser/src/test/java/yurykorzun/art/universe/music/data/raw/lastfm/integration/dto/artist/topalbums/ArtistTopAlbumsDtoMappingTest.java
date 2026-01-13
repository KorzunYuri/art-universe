package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.artist.topalbums;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.test.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistTopAlbumsDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenArtistTopAlbumsResponseProvided() throws IOException {

        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopAlbums");
        ArtistTopAlbumsDtoRoot dtoRoot = mapper.readValue(responseJsonString, ArtistTopAlbumsDtoRoot.class);

        assertNotNull(dtoRoot);

        ArtistTopAlbumsTopAlbumsDto rootObject = dtoRoot.getTopAlbumsObject();
        assertNotNull(rootObject);

        List<ArtistTopAlbumsAlbumDto> albums = rootObject.getAlbums();
        assertNotNull(albums);
        assertEquals(50, albums.size());

        ArtistTopAlbumsAlbumDto album = albums.get(0);
        assertNotNull(album);
        assertEquals("Monolith of Inhumanity", album.getName());
        assertEquals("2967065a-a2b0-4a16-9fa9-f3169dcd0529", album.getMbid());
        assertEquals("https://www.last.fm/music/Cattle+Decapitation/Monolith+of+Inhumanity", album.getUrl());
        assertEquals(2341387, album.getPlayCount());
    }
}
