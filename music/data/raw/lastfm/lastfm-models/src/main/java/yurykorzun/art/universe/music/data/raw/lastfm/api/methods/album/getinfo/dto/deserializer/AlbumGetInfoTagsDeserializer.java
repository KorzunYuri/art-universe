package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.dto.AlbumGetInfoTagsDto;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Custom deserializer for AlbumGetInfoTagsDto that handles cases where:
 * 1. tags field is an empty string "" instead of an object
 * 2. tags field is a proper object with tag array
 */
public class AlbumGetInfoTagsDeserializer extends JsonDeserializer<AlbumGetInfoTagsDto> {

    @Override
    public AlbumGetInfoTagsDto deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        AlbumGetInfoTagsDto tagsDto = new AlbumGetInfoTagsDto();
        
        JsonToken token = parser.getCurrentToken();
        
        if (token == JsonToken.VALUE_STRING) {
            // Handle empty string case: "tags": ""
            String value = parser.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                // Return empty tags object
                tagsDto.setTags(Collections.emptyList());
                return tagsDto;
            }
        } else if (token == JsonToken.START_OBJECT) {
            // Handle normal object case: "tags": { "tag": [...] }
            ObjectMapper mapper = (ObjectMapper) parser.getCodec();
            
            while (parser.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                
                if ("tag".equals(fieldName)) {
                    if (parser.getCurrentToken() == JsonToken.START_ARRAY) {
                        // Array of tags
                        List<AlbumGetInfoTagDto> tags = mapper.readValue(parser, 
                            mapper.getTypeFactory().constructCollectionType(List.class, AlbumGetInfoTagDto.class));
                        tagsDto.setTags(tags);
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
            // For any other token type, return empty tags
            tagsDto.setTags(Collections.emptyList());
        }
        
        return tagsDto;
    }
}
