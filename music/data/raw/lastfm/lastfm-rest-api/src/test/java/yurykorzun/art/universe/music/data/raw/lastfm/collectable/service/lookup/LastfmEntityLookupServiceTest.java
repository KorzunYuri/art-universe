package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.lookup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.common.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Import({
    DbConsistencyHelper.class
})
class LastfmEntityLookupServiceTest extends LastfmJpaTestHelper {

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @Test
    void lookup_shouldWorkForArtists_whenUsingCommonService() {
        // Given
        dbHelper.cleanup();
        
        LastfmEntityLookupService artistLookupService = new LastfmEntityLookupService(entityManager, LastfmEntityType.ARTIST);
        
        LastfmArtist artist1 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmArtist artist2 = dbHelper.createAndSaveArtist(builder -> builder
            .name("Another Artist")
            .approvalStatus(ApprovalStatus.PENDING));

        dbHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("Artist")
            .limit(10)
            .build();
        
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Approved artist should come first
        assertEquals(artist1.getId(), results.get(0).getId());
        assertEquals(artist2.getId(), results.get(1).getId());
    }

    @Test
    void lookup_shouldWorkForTags_whenUsingCommonService() {
        // Given
        dbHelper.cleanup();
        
        LastfmEntityLookupService tagLookupService = new LastfmEntityLookupService(entityManager, LastfmEntityType.TAG);
        
        LastfmTag tag1 = dbHelper.createAndSaveTag(builder -> builder
            .name("rock")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmTag tag2 = dbHelper.createAndSaveTag(builder -> builder
            .name("rock music")
            .approvalStatus(ApprovalStatus.PENDING));

        dbHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("rock")
            .limit(10)
            .build();
        
        List<LookupResultDTO> results = tagLookupService.lookup(request);

        // Then
        assertNotNull(results);
        assertEquals(2, results.size());
        
        // Exact match should come first despite lower approval status
        assertEquals(tag1.getId(), results.get(0).getId());
        assertEquals(tag2.getId(), results.get(1).getId());
    }

    @Test
    void lookup_shouldReturnEmptyList_whenNoMatches() {
        // Given
        dbHelper.cleanup();
        
        LastfmEntityLookupService artistLookupService = new LastfmEntityLookupService(entityManager, LastfmEntityType.ARTIST);
        
        dbHelper.createAndSaveArtist(builder -> builder
            .name("Completely Different Name")
            .approvalStatus(ApprovalStatus.APPROVED));

        dbHelper.flush();

        // When
        LookupRequestDTO request = LookupRequestDTO.builder()
            .search("NonExistent")
            .limit(10)
            .build();
        
        List<LookupResultDTO> results = artistLookupService.lookup(request);

        // Then
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }
}
