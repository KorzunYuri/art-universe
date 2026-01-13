package yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.tag.toptags;

import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.test.utils.LastfmApiClientResourceUtil;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TagTopTagsDtoMappingTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void parse_shouldParseCorrectly_whenTagTopTagsResponseProvided() throws IOException {
        String responseJsonString = LastfmApiClientResourceUtil.getApiClientResponse("tag.getTopTags");
        TagTopTagsDtoRoot tagTopTagsDtoRoot = mapper.readValue(responseJsonString, TagTopTagsDtoRoot.class);

        assertNotNull(tagTopTagsDtoRoot);

        TagTopTagsTagsDto tagTopTagsTagsDto = tagTopTagsDtoRoot.getTopTags();
        assertNotNull(tagTopTagsTagsDto);

        PageInfo pageInfo = tagTopTagsTagsDto.getPageInfo();
        assertNotNull(pageInfo);
        assertEquals(0, pageInfo.getOffset());
        assertEquals(50, pageInfo.getCount());
        assertEquals(2855332, pageInfo.getTotal());

        List<TagTopTagsTagDto> tags = tagTopTagsTagsDto.getTags();
        assertNotNull(tags);
        assertEquals(50, tags.size());

        TagTopTagsTagDto tagDto = tags.get(0);
        assertNotNull(tagDto);
        assertEquals("rock", tagDto.getName());
        assertEquals(4050770, tagDto.getUsageCount());
        assertEquals(401715, tagDto.getUsageUsersCount());
    }
}
