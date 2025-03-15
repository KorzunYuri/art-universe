package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.api.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TagToptagsMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    public void givenTagTopTagsResponse_whenParsed_thenParsedCorrectly() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("tag.topTags");
        TopTagsDtoRoot topTagsDtoRoot = mapper.readValue(responseJsonString, TopTagsDtoRoot.class);

        assertNotNull(topTagsDtoRoot);

        TopTagsDto topTagsDto = topTagsDtoRoot.getTopTags();
        assertNotNull(topTagsDto);

        PageInfo pageInfo = topTagsDto.getPageInfo();
        assertNotNull(pageInfo);
        assertEquals(0, pageInfo.getOffset());
        assertEquals(50, pageInfo.getCount());
        assertEquals(2855332, pageInfo.getTotal());

        List<TagDto> tags = topTagsDto.getTags();
        assertNotNull(tags);
        assertEquals(50, tags.size());

        TagDto tagDto = tags.get(0);
        assertNotNull(tagDto);
        assertEquals("rock", tagDto.getName());
        assertEquals(4050770, tagDto.getCount());
        assertEquals(401715, tagDto.getReach());
    }
}
