package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.dto;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TrackGetInfoDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenTrackGetInfoResponseProvided() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("track.getInfo");

        TrackGetInfoDtoRoot root = mapper.readValue(responseJsonString, TrackGetInfoDtoRoot.class);
        assertNotNull(root);

        // Check track
        TrackGetInfoTrackDto track = root.getTrack();
        assertNotNull(track);
        assertNotNull(track.getName());
        assertNotNull(track.getMbid());
        assertNotNull(track.getUrl());
        assertTrue(track.getDuration() > 0);
        assertTrue(track.getListeners() > 0);
        assertTrue(track.getPlaycount() > 0);
        
        // Check artist
        TrackGetInfoArtistDto artist = track.getArtist();
        assertNotNull(artist);
        assertNotNull(artist.getName());
        assertNotNull(artist.getMbid());
        assertNotNull(artist.getUrl());
        
        // Check album if present
        TrackGetInfoAlbumDto album = track.getAlbum();
        if (album != null) {
            assertNotNull(album.getName());
            assertNotNull(album.getUrl());
            assertNotNull(album.getMbid());

            // Check album artist if present
            if (album.getArtistName() != null) {
                assertNotNull(album.getArtistName());
            }
        }
        
        // Check tags
        TrackGetInfoTagsDto tagsObject = track.getTopTags();
        assertNotNull(tagsObject);
        List<TrackGetInfoTagDto> tags = tagsObject.getTags();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        
        // Check first tag
        TrackGetInfoTagDto firstTag = tags.get(0);
        assertNotNull(firstTag.getName());
        assertNotNull(firstTag.getUrl());
    }
}
