package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.lookup;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupRequestDTO;
import yurykorzun.art.universe.common.domain.dto.lookup.LookupResultDTO;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmEntityLookupServiceTest extends LastfmJpaTestHelper {

    @Autowired
    private EntityManager entityManager;

    @Test
    void lookup_shouldWorkForArtists_whenUsingCommonService() {
        // Given
        LastfmEntityLookupService artistLookupService = new LastfmEntityLookupService(entityManager, LastfmEntityType.ARTIST);
        
        LastfmArtist artist1 = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Test Artist")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmArtist artist2 = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Another Artist")
            .approvalStatus(ApprovalStatus.PENDING));

        consistencyHelper.flush();

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
        consistencyHelper.cleanup();
        
        LastfmEntityLookupService tagLookupService = new LastfmEntityLookupService(entityManager, LastfmEntityType.TAG);
        
        LastfmTag tag1 = consistencyHelper.createAndSaveTag(builder -> builder
            .name("rock")
            .approvalStatus(ApprovalStatus.APPROVED));
        
        LastfmTag tag2 = consistencyHelper.createAndSaveTag(builder -> builder
            .name("rock music")
            .approvalStatus(ApprovalStatus.PENDING));

        consistencyHelper.flush();

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
        consistencyHelper.cleanup();
        
        LastfmEntityLookupService artistLookupService = new LastfmEntityLookupService(entityManager, LastfmEntityType.ARTIST);
        
        consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Completely Different Name")
            .approvalStatus(ApprovalStatus.APPROVED));

        consistencyHelper.flush();

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
