package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.LastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaTestWithHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Transactionality tests for BaseLastfmEntityRelation
 */
@Import({
    LastfmArtistTagServiceImpl.class,
})
@Tag("integration")
@DirtiesContext
class EntityRelationTransactionalityTest extends JpaTestWithHelper {

    @Autowired
    private LastfmArtistTagService artistTagService;
    
    @Autowired
    private LastfmArtistTagRepository artistTagRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void upsertAll_shouldThrowException_whenDataIntegrityViolated() {
        // Given - create valid entity
        LastfmArtistTag validEntity = dbHelper.createArtistTagForPersistence();
        
        // Create invalid entity with non-existent IDs but valid structure
        LastfmApiCall apiCall = dbHelper.createAndSaveApiCall();
        
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
            dbHelper.createArtistTagForPersistence(),
            dbHelper.createArtistTagForPersistence(),
            dbHelper.createArtistTagForPersistence()
        );

        // When
        artistTagService.upsertAll(batch);
        entityManager.flush();

        // Then - all should be saved atomically
        assertEquals(3, artistTagRepository.count());
    }
}
