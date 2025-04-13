package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsArtistMetadata;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsRootDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsTagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.dto.ArtistTopTagsTopTagsDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ArtistTopTagsMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void givenTagTopTagsResponse_whenParsed_thenParsedCorrectly() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("artist.getTopTags");
        ArtistTopTagsRootDto dtoRoot = mapper.readValue(responseJsonString, ArtistTopTagsRootDto.class);

        assertNotNull(dtoRoot);

        ArtistTopTagsTopTagsDto topTagsObject = dtoRoot.getTopTagsObject();
        assertNotNull(topTagsObject);

        ArtistTopTagsArtistMetadata artistMetadata = topTagsObject.getArtist();
        assertNotNull(artistMetadata);
        assertEquals("Cattle Decapitation", artistMetadata.getName());

        List<ArtistTopTagsTagDto> tagDtos = topTagsObject.getTags();
        assertNotNull(tagDtos);
        assertEquals(28, tagDtos.size());

        ArtistTopTagsTagDto tagDto = tagDtos.get(0);
        assertNotNull(tagDto);
        assertEquals(100, tagDto.getUsageCount());
        assertEquals("grindcore", tagDto.getName());
        assertEquals("https://www.last.fm/tag/grindcore", tagDto.getUrl());
    }
}
