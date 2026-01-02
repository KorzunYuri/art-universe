package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityRelationType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.relationship.LastfmArtistsRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.TestLastfmArtistAlbumRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.TestLastfmArtistTagRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.relationship.TestLastfmArtistsRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistAlbumServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistTagServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.relationship.impl.LastfmArtistsRelationServiceImpl;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for the unified entity relation services
 */
@Import({
    LastfmArtistTagServiceImpl.class,
    LastfmArtistsRelationServiceImpl.class,
    LastfmArtistAlbumServiceImpl.class,
    DbConsistencyHelper.class
})
class UnifiedEntityRelationIntegrationTest extends LastfmJpaTestHelper {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private LastfmArtistTagService artistTagService;
    
    @Autowired
    private LastfmArtistsRelationService artistsRelationService;
    
    @Autowired
    private LastfmArtistAlbumService artistAlbumService;

    @Autowired
    private TestLastfmArtistTagRepository artistTagRepository;
    
    @Autowired
    private TestLastfmArtistsRelationRepository artistsRelationRepository;
    
    @Autowired
    private TestLastfmArtistAlbumRepository artistAlbumRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void upsertAll_shouldWorkForDifferentEntityTypes() {
        // Given - ArtistTag (different entity types)
        LastfmArtist artist = dbHelper.createAndSaveArtist();
        LastfmTag tag = dbHelper.createAndSaveTag();
        
        LastfmArtistTag artistTag = LastfmArtistTag.builder()
                .artist(artist)
                .tag(tag)
                .usageCount(100)
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();

        // When
        artistTagService.upsertAll(List.of(artistTag));

        // Then
        assertEquals(1, artistTagRepository.count());
        
        LastfmArtistTag saved = artistTagRepository.findAll().get(0);
        assertEquals(artist.getId(), saved.getArtist().getId());
        assertEquals(tag.getId(), saved.getTag().getId());
        assertEquals(100, saved.getUsageCount());
    }

    @Test
    void upsertAll_shouldWorkForSameEntityTypes() {
        // Given - ArtistsRelation (same entity types)
        LastfmArtist sourceArtist = dbHelper.createAndSaveArtist();
        LastfmArtist targetArtist = dbHelper.createAndSaveArtist();
        
        LastfmArtistsRelation artistsRelation = LastfmArtistsRelation.builder()
                .sourceArtist(sourceArtist)
                .targetArtist(targetArtist)
                .matchScore(BigDecimal.valueOf(0.85))
                .relationType(LastfmEntityRelationType.SIMILARITY)
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();

        // When
        artistsRelationService.upsertAll(List.of(artistsRelation));

        // Then
        assertEquals(1, artistsRelationRepository.count());
        
        LastfmArtistsRelation saved = artistsRelationRepository.findAll().get(0);
        assertEquals(sourceArtist.getId(), saved.getSourceArtist().getId());
        assertEquals(targetArtist.getId(), saved.getTargetArtist().getId());
        assertEquals(0, BigDecimal.valueOf(0.85).compareTo(saved.getMatchScore()));
        assertEquals(LastfmEntityRelationType.SIMILARITY, saved.getRelationType());
    }

    @Test
    void upsertAll_shouldWorkForNoUpdatableFields() {
        // Given - ArtistAlbum (no updatable fields)
        LastfmArtist artist = dbHelper.createAndSaveArtist();
        LastfmAlbum album = dbHelper.createAndSaveAlbum();
        
        LastfmArtistAlbum artistAlbum = LastfmArtistAlbum.builder()
                .artist(artist)
                .album(album)
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();

        // When
        artistAlbumService.upsertAll(List.of(artistAlbum));

        // Then
        assertEquals(1, artistAlbumRepository.count());
        
        LastfmArtistAlbum saved = artistAlbumRepository.findAll().get(0);
        assertEquals(artist.getId(), saved.getArtist().getId());
        assertEquals(album.getId(), saved.getAlbum().getId());
    }

    @Test
    void upsertAll_shouldHandleConflictsCorrectly() {
        // Given - Create initial ArtistTag
        LastfmArtist artist = dbHelper.createAndSaveArtist();
        LastfmTag tag = dbHelper.createAndSaveTag();
        
        LastfmArtistTag initialArtistTag = LastfmArtistTag.builder()
                .artist(artist)
                .tag(tag)
                .usageCount(100)
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();
        
        artistTagService.upsertAll(List.of(initialArtistTag));
        assertEquals(1, artistTagRepository.count());

        // When - Upsert with updated values
        LastfmArtistTag updatedArtistTag = LastfmArtistTag.builder()
                .artist(artist)
                .tag(tag)
                .usageCount(200) // Updated
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();
        
        artistTagService.upsertAll(List.of(updatedArtistTag));

        // Then - Should still have only one record with updated values
        assertEquals(1, artistTagRepository.count());
        
        LastfmArtistTag saved = artistTagRepository.findAll().get(0);
        assertEquals(200, saved.getUsageCount()); // Should be updated due to COALESCE logic
    }

    @Test
    void upsertAll_shouldHandleSameEntityConflictsCorrectly() {
        // Given - Create initial ArtistsRelation
        LastfmArtist sourceArtist = dbHelper.createAndSaveArtist();
        LastfmArtist targetArtist = dbHelper.createAndSaveArtist();
        
        LastfmArtistsRelation initialRelation = LastfmArtistsRelation.builder()
                .sourceArtist(sourceArtist)
                .targetArtist(targetArtist)
                .matchScore(BigDecimal.valueOf(0.75))
                .relationType(LastfmEntityRelationType.SIMILARITY)
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();
        
        artistsRelationService.upsertAll(List.of(initialRelation));
        assertEquals(1, artistsRelationRepository.count());

        // When - Upsert with updated match score
        LastfmArtistsRelation updatedRelation = LastfmArtistsRelation.builder()
                .sourceArtist(sourceArtist)
                .targetArtist(targetArtist)
                .matchScore(BigDecimal.valueOf(0.90)) // Updated
                .relationType(LastfmEntityRelationType.SIMILARITY)
                .apiCall(dbHelper.createAndSaveApiCall())
                .build();
        entityManager.flush();
        
        artistsRelationService.upsertAll(List.of(updatedRelation));

        // Then - Should still have only one record with updated match score
        assertEquals(1, artistsRelationRepository.count());
        
        LastfmArtistsRelation saved = artistsRelationRepository.findAll().get(0);
        assertEquals(0, BigDecimal.valueOf(0.90).compareTo(saved.getMatchScore())); // Should be updated due to COALESCE logic
    }

    @Test
    void allServices_shouldBeAutowiredSuccessfully() {
        // Then - All services should be created and autowired
        assertNotNull(artistTagService, "ArtistTagService should be autowired");
        assertNotNull(artistsRelationService, "ArtistsRelationService should be autowired");
        assertNotNull(artistAlbumService, "ArtistAlbumService should be autowired");
        
        // Verify they are the correct implementation types
        assertTrue(artistTagService instanceof LastfmArtistTagServiceImpl, 
                "Should be the unified implementation");
        assertTrue(artistsRelationService instanceof LastfmArtistsRelationServiceImpl, 
                "Should be the unified implementation");
        assertTrue(artistAlbumService instanceof LastfmArtistAlbumServiceImpl, 
                "Should be the unified implementation");
    }
}
