package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto;

import org.junit.jupiter.api.Test;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AlbumGetInfoDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenAlbumGetInfoResponseProvided() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("album.getInfo");

        AlbumGetInfoDtoRoot root = mapper.readValue(responseJsonString, AlbumGetInfoDtoRoot.class);
        assertNotNull(root);

        // Check album
        AlbumGetInfoAlbumDto album = root.getAlbum();
        assertNotNull(album);
        assertNotNull(album.getName());
        assertNotNull(album.getMbid());
        assertNotNull(album.getUrl());
        assertNotNull(album.getArtist());
        assertTrue(album.getPlayCount() > 0);
        assertTrue(album.getListenersCount() > 0);
        
        // Check tracks
        AlbumGetInfoTracksDto tracksObject = album.getTracksObject();
        assertNotNull(tracksObject);
        List<AlbumGetInfoTrackDto> tracks = tracksObject.getTracks();
        assertNotNull(tracks);
        assertFalse(tracks.isEmpty());
        
        // Check first track
        AlbumGetInfoTrackDto firstTrack = tracks.get(0);
        assertNotNull(firstTrack.getName());
        assertNotNull(firstTrack.getUrl());
        assertTrue(firstTrack.getDuration() > 0);
        
        // Check track artist
        assertNotNull(firstTrack.getArtist());
        assertNotNull(firstTrack.getArtist().getName());
        assertNotNull(firstTrack.getArtist().getUrl());
        
        // Check track position
        assertNotNull(firstTrack.getAttr());
        assertTrue(firstTrack.getAttr().getRank() > 0);
        
        // Check tags
        AlbumGetInfoTagsDto tagsObject = album.getTags();
        assertNotNull(tagsObject);
        List<AlbumGetInfoTagDto> tags = tagsObject.getTag();
        assertNotNull(tags);
        assertFalse(tags.isEmpty());
        
        // Check first tag
        AlbumGetInfoTagDto firstTag = tags.get(0);
        assertNotNull(firstTag.getName());
        assertNotNull(firstTag.getUrl());
    }
}
