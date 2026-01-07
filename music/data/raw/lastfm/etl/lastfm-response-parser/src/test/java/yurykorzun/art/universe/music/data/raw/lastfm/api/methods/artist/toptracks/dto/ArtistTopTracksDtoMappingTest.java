package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.dto;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ArtistTopTracksDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenArtistTopTracksResponseProvided() throws IOException {

        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTracks");
        ArtistTopTracksDtoRoot dtoRoot = mapper.readValue(responseJsonString, ArtistTopTracksDtoRoot.class);

        assertNotNull(dtoRoot);

        ArtistTopTracksTopTracksDto rootObject = dtoRoot.getRootObject();
        assertNotNull(rootObject);

        ArtistTopTracksRequestMetadataDto requestMetadata = rootObject.getArtistMetadata();
        assertEquals("Cattle Decapitation", requestMetadata.getArtistName());
        assertEquals(1, requestMetadata.getPageNumber());
        assertEquals(50, requestMetadata.getPageSize());
        assertEquals(118, requestMetadata.getPagesTotal());
        assertEquals(5890, requestMetadata.getTracksTotal());

        List<ArtistTopTracksTrackDto> tracks = rootObject.getTracks();
        assertNotNull(tracks);
        assertEquals(50, tracks.size());

        ArtistTopTracksTrackDto track = tracks.get(0);
        assertNotNull(track);
        assertEquals("Forced Gender Reassignment", track.getName());
        assertEquals("072f6f72-516d-48d9-a8d8-66ce6cd2a315", track.getMbid());
        assertEquals("https://www.last.fm/music/Cattle+Decapitation/_/Forced+Gender+Reassignment", track.getUrl());
        assertEquals(458744, track.getPlayCount());
        assertEquals(58863, track.getListenersCount());
        assertEquals(0, track.getStreamable());
    }

}