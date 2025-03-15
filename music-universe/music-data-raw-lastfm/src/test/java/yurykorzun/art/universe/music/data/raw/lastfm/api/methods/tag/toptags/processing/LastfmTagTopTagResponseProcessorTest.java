package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
public class LastfmTagTopTagResponseProcessorTest {

    @Autowired
    private LastfmTagTopTagResponseProcessor processor;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @BeforeEach
    public void cleanDatabase() {
        tagRepository.deleteAll();
        attributeHistoryRepository.deleteAll();
    }

    @Test
    @Transactional
    public void testInitialSave() {
        TopTagsDtoRoot dto = createTestDtoRoot();

        processor.processParsedResponse(dto);

        List<LastfmTag> tags = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributes = attributeHistoryRepository.findAll();

        assertEquals(2, tags.size(), "Must save all tags");
        assertEquals(6, attributes.size(), "Must save (tags X (tag attributes)) attribute records");
    }

    @Test
    @Transactional
    public void testSecondarySave() {
        TopTagsDtoRoot dto = createTestDtoRoot();

        // initial save
        processor.processParsedResponse(dto);
        List<LastfmTag> tagsAfterFirst = tagRepository.findAll();

        // secondary save
        processor.processParsedResponse(dto);
        List<LastfmTag> tagsAfterSecond = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterSecond = attributeHistoryRepository.findAll();

        assertEquals(tagsAfterFirst.size(), tagsAfterSecond.size(), "Tags must not duplicate on second save");
        assertEquals(2, tagsAfterSecond.size(), "Must save all tags");
        assertEquals(12, attributesAfterSecond.size(),
                "Must save (tags X (tag attributes)) attribute records on each save");
    }

    private TopTagsDtoRoot createTestDtoRoot() {
        TopTagsDtoRoot root = new TopTagsDtoRoot();
        TopTagsDto topTags = new TopTagsDto();
        PageInfo pageInfo = new PageInfo();
        pageInfo.setOffset(0);
        pageInfo.setCount(2);
        pageInfo.setTotal(2);
        topTags.setPageInfo(pageInfo);

        TagDto tag1 = new TagDto();
        tag1.setName("rock");
        tag1.setCount(4050770);
        tag1.setReach(401715);

        TagDto tag2 = new TagDto();
        tag2.setName("electronic");
        tag2.setCount(2475740);
        tag2.setReach(260815);

        topTags.setTags(List.of(tag1, tag2));
        root.setTopTags(topTags);
        return root;
    }
}