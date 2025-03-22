package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.processing;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.PageInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.dto.TagDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDto;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.dto.TopTagsDtoRoot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository.LastfmAttributeHistoryRecordRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository.LastfmTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.FullContextTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Slf4j
public class LastfmTagTopTagResponseProcessorTest extends FullContextTest {

    @Autowired
    private LastfmTagTopTagResponseProcessor processor;

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmAttributeHistoryRecordRepository attributeHistoryRepository;

    @AfterEach
    public void reset() {
        cleanDatabase();
    }

    @AfterEach
    public void cleanDatabase() {
        tagRepository.deleteAll();
        attributeHistoryRepository.deleteAll();
    }

    @Test
    public void testFirstSave() {
        log.info("testFirstSave START");
        TopTagsDtoRoot dto = createTestDtoRoot();
        final int tagsNumber = dto.getTopTags().getTags().size();
        final int attributesNumber = 3;

        // initial save
        processor.processParsedResponse(dto);
        List<LastfmTag> tagsAfterFirst = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterFirst = attributeHistoryRepository.findAll();

        assertEquals(tagsNumber, tagsAfterFirst.size(),
                "First save must save tags that haven't existed in DB");
        assertEquals(tagsNumber * attributesNumber, attributesAfterFirst.size(),
                "First save must save (tags X (tag attributes)) attribute records");
        log.info("testFirstSave FINISH");
    }

    @Test
    public void testSecondarySave() {
        log.info("testSecondarySave START");
        TopTagsDtoRoot dto = createTestDtoRoot();
        final int tagsNumber = dto.getTopTags().getTags().size();
        final int attributesNumber = 3;

        // initial save
        processor.processParsedResponse(dto);
        List<LastfmTag> tagsAfterFirst = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterFirst = attributeHistoryRepository.findAll();

        assertEquals(tagsNumber, tagsAfterFirst.size(),
                "First save must save tags that haven't existed in DB");
        assertEquals(tagsNumber * attributesNumber, attributesAfterFirst.size(),
                "First save must save (tags X (tag attributes)) attribute records");

        // secondary save
        processor.processParsedResponse(dto);
        List<LastfmTag> tagsAfterSecond = tagRepository.findAll();
        List<LastfmAttributeHistoryRecord> attributesAfterSecond = attributeHistoryRepository.findAll();

        assertEquals(tagsAfterFirst.size(), tagsAfterSecond.size(), "Second save must not produce duplicate tags");
        assertEquals(tagsNumber * attributesNumber * 2, attributesAfterSecond.size(),
                "Second save must produce new tag attribute values (tags X (tag attributes)) records");
        log.info("testSecondarySave FINISH");
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