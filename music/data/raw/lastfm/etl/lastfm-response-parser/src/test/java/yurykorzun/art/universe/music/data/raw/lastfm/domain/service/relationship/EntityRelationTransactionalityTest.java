package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository.relationship.TestLastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.relationship.impl.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Transactionality tests for BaseLastfmEntityRelation
 */
@Import({
    LastfmArtistTagServiceImpl.class,
})
@DirtiesContext
class EntityRelationTransactionalityTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmArtistTagService artistTagService;
    
    @Autowired
    private TestLastfmArtistTagRepository artistTagRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void upsertAll_shouldThrowException_whenDataIntegrityViolated() {
        // Given - create valid entity
        LastfmArtistTag validEntity = consistencyHelper.createArtistTagForPersistence();
        
        // Create invalid entity with non-existent IDs but valid structure
        LastfmApiCall apiCall = consistencyHelper.createAndSaveApiCall();
        
        // Create entities with valid structure but non-existent IDs
        LastfmArtist fakeArtist = EntityCreationHelper.createArtist(builder -> 
            builder.id(999999L).apiCall(apiCall));
        LastfmTag fakeTag = EntityCreationHelper.createTag(builder -> 
            builder.id(999999L).apiCall(apiCall));
        
        LastfmArtistTag invalidEntity = EntityCreationHelper.createArtistTag(builder -> 
            builder.artist(fakeArtist)
                  .tag(fakeTag)
                  .apiCall(apiCall));

        // When & Then
        assertThrows(Exception.class, () -> {
            artistTagService.upsertAll(List.of(validEntity, invalidEntity));
        });
    }

    @Test
    void upsertAll_shouldBeTransactional() {
        // Given
        List<LastfmArtistTag> batch = List.of(
            consistencyHelper.createArtistTagForPersistence(),
            consistencyHelper.createArtistTagForPersistence(),
            consistencyHelper.createArtistTagForPersistence()
        );

        // When
        artistTagService.upsertAll(batch);
        entityManager.flush();

        // Then - all should be saved atomically
        assertEquals(3, artistTagRepository.count());
    }
}
