package yurykorzun.art.universe.music.data.raw.lastfm.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yurykorzun.art.universe.common.persistence.entity.RequestStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.entity.TagsRequest;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.transaction.Transactional;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static yurykorzun.art.universe.music.data.raw.lastfm.utils.TimeTestUtils.*;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
public class TagsRequestRepositoryTest {

    @Autowired
    private TagsRequestRepository tagsRequestRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    public void givenEmptyTable_whenTagsRequestInserted_thenTagsRequestPersisted() {
        // given
        Instant from = now();
        Instant to = now().plus(1, ChronoUnit.MINUTES);
        TagsRequest request = new TagsRequest(from, to);

        // when
        request = tagsRequestRepository.save(request);

        // then
        Optional<TagsRequest> optPersisted = tagsRequestRepository.findById(request.getId());
        assertTrue(optPersisted.isPresent());
        TagsRequest persisted = optPersisted.get();
        assertEquals(from, persisted.getFromDttm());
        assertEquals(to, persisted.getToDttm());
        assertEquals(request.getId(), persisted.getId());
        assertEquals(request.getStatus(), persisted.getStatus());
        assertEquals(request.getStatus(), RequestStatus.CREATED);

        assertEquals(1, tagsRequestRepository.count());
    }

    @Test
    @Transactional
    public void givenEmptyTable_whenTagsRequestInserted_thenTagsRequestHistoryUpdated() {
        // given
        Instant from = now();
        Instant to = now().plus(1, ChronoUnit.MINUTES);
        TagsRequest request = new TagsRequest(from, to);

        // when
        tagsRequestRepository.save(request);
        entityManager.flush();

        // then
        Query query = entityManager.createNativeQuery("""
            SELECT  COUNT(*) 
            FROM    tags_request_history 
            WHERE   tag_request_id = :id
        """);
        query.setParameter("id", request.getId());
        Number count = (Number) query.getSingleResult();

        assertEquals(1, count.intValue());
    }
}
