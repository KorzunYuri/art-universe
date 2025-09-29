package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTrackDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTracksDto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Custom deserializer for AlbumGetInfoTracksDto that handles cases where:
 * 1. track field is an array of tracks (normal case)
 * 2. track field is a single track object (when album has only one track)
 */
public class AlbumGetInfoTracksDeserializer extends JsonDeserializer<AlbumGetInfoTracksDto> {

    @Override
    public AlbumGetInfoTracksDto deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        AlbumGetInfoTracksDto tracksDto = new AlbumGetInfoTracksDto();
        
        JsonToken token = parser.getCurrentToken();
        
        if (token == JsonToken.START_OBJECT) {
            // Handle object case: "tracks": { "track": {...} or [...] }
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                
                if ("track".equals(fieldName)) {
                    JsonToken trackToken = parser.getCurrentToken();
                    
                    if (trackToken == JsonToken.START_ARRAY) {
                        // Array of tracks (normal case)
                        List<AlbumGetInfoTrackDto> tracks = mapper.readValue(parser, 
                            mapper.getTypeFactory().constructCollectionType(List.class, AlbumGetInfoTrackDto.class));
                        tracksDto.setTracks(tracks);
                    } else if (trackToken == JsonToken.START_OBJECT) {
                        // Single track object
                        AlbumGetInfoTrackDto track = mapper.readValue(parser, AlbumGetInfoTrackDto.class);
                        List<AlbumGetInfoTrackDto> tracks = new ArrayList<>();
                        tracks.add(track);
                        tracksDto.setTracks(tracks);
                    } else {
                        // Skip other tokens
                        parser.skipChildren();
                    }
                } else {
                    // Skip unknown fields
                    parser.skipChildren();
                }
            }
        } else {
            // For any other token type, return empty tracks
            tracksDto.setTracks(Collections.emptyList());
        }
        
        return tracksDto;
    }
}
