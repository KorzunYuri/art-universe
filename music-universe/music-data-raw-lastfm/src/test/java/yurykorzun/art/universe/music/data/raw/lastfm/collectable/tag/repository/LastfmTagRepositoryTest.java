package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityRelation;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository.LastfmEntityRelationRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmTagRepository tagRepository;

    @Autowired
    private LastfmEntityRelationRepository entityRelationRepository;

    @Autowired
    private DbConsistencyHelper dbHelper;

    @BeforeEach
    void setUp() {
        dbHelper.cleanup();
    }

    @Test
    void findTagsByEntity_shouldReturnTagsOrderedAlphabetically() {
        // Given
        LastfmArtist artist = dbHelper.createAndSaveArtist();
        
        // Create tags with names that will test alphabetical ordering
        LastfmTag tagZebra = dbHelper.createAndSaveTag();
        LastfmTag tagApple = dbHelper.createAndSaveTag();
        LastfmTag tagBanana = dbHelper.createAndSaveTag();

        // Update tag names
        tagZebra = tagRepository.save(LastfmTag.builder()
            .id(tagZebra.getId())
            .name("zebra")
            .url(tagZebra.getUrl())
            .apiCall(tagZebra.getApiCall())
            .approvalStatus(tagZebra.getApprovalStatus())
            .createdAt(tagZebra.getCreatedAt())
            .updatedAt(tagZebra.getUpdatedAt())
            .build());
            
        tagApple = tagRepository.save(LastfmTag.builder()
            .id(tagApple.getId())
            .name("apple")
            .url(tagApple.getUrl())
            .apiCall(tagApple.getApiCall())
            .approvalStatus(tagApple.getApprovalStatus())
            .createdAt(tagApple.getCreatedAt())
            .updatedAt(tagApple.getUpdatedAt())
            .build());
            
        tagBanana = tagRepository.save(LastfmTag.builder()
            .id(tagBanana.getId())
            .name("banana")
            .url(tagBanana.getUrl())
            .apiCall(tagBanana.getApiCall())
            .approvalStatus(tagBanana.getApprovalStatus())
            .createdAt(tagBanana.getCreatedAt())
            .updatedAt(tagBanana.getUpdatedAt())
            .build());

        // Create entity relations
        entityRelationRepository.save(LastfmEntityRelation.builder()
            .scopeEntityType(LastfmEntityType.ARTIST)
            .scopeEntityId(artist.getId())
            .entityType(LastfmEntityType.TAG)
            .entityId(tagZebra.getId())
            .apiCall(dbHelper.createAndSaveApiCall())
            .build());
            
        entityRelationRepository.save(LastfmEntityRelation.builder()
            .scopeEntityType(LastfmEntityType.ARTIST)
            .scopeEntityId(artist.getId())
            .entityType(LastfmEntityType.TAG)
            .entityId(tagApple.getId())
            .apiCall(dbHelper.createAndSaveApiCall())
            .build());
            
        entityRelationRepository.save(LastfmEntityRelation.builder()
            .scopeEntityType(LastfmEntityType.ARTIST)
            .scopeEntityId(artist.getId())
            .entityType(LastfmEntityType.TAG)
            .entityId(tagBanana.getId())
            .apiCall(dbHelper.createAndSaveApiCall())
            .build());

        // When
        List<LastfmTag> result = tagRepository.findTagsByEntity(LastfmEntityType.ARTIST, artist.getId());

        // Then
        assertNotNull(result);
        assertEquals(3, result.size());
        
        // Verify alphabetical ordering
        assertEquals("apple", result.get(0).getName());
        assertEquals("banana", result.get(1).getName());
        assertEquals("zebra", result.get(2).getName());
        
        // Verify correct tag IDs
        assertEquals(tagApple.getId(), result.get(0).getId());
        assertEquals(tagBanana.getId(), result.get(1).getId());
        assertEquals(tagZebra.getId(), result.get(2).getId());
    }

    @Test
    void findTagsByEntity_shouldReturnEmptyListWhenNoTagsFound() {
        // Given
        LastfmArtist artist = dbHelper.createAndSaveArtist();

        // When
        List<LastfmTag> result = tagRepository.findTagsByEntity(LastfmEntityType.ARTIST, artist.getId());

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void findTagsByEntity_shouldReturnOnlyTagsForSpecificEntity() {
        // Given
        LastfmArtist artist1 = dbHelper.createAndSaveArtist();
        LastfmArtist artist2 = dbHelper.createAndSaveArtist();
        
        LastfmTag tag1 = dbHelper.createAndSaveTag();
        LastfmTag tag2 = dbHelper.createAndSaveTag();

        // Create relation for artist1 with tag1
        entityRelationRepository.save(LastfmEntityRelation.builder()
            .scopeEntityType(LastfmEntityType.ARTIST)
            .scopeEntityId(artist1.getId())
            .entityType(LastfmEntityType.TAG)
            .entityId(tag1.getId())
            .apiCall(dbHelper.createAndSaveApiCall())
            .build());
            
        // Create relation for artist2 with tag2
        entityRelationRepository.save(LastfmEntityRelation.builder()
            .scopeEntityType(LastfmEntityType.ARTIST)
            .scopeEntityId(artist2.getId())
            .entityType(LastfmEntityType.TAG)
            .entityId(tag2.getId())
            .apiCall(dbHelper.createAndSaveApiCall())
            .build());

        // When
        List<LastfmTag> result1 = tagRepository.findTagsByEntity(LastfmEntityType.ARTIST, artist1.getId());
        List<LastfmTag> result2 = tagRepository.findTagsByEntity(LastfmEntityType.ARTIST, artist2.getId());

        // Then
        assertEquals(1, result1.size());
        assertEquals(tag1.getId(), result1.get(0).getId());
        
        assertEquals(1, result2.size());
        assertEquals(tag2.getId(), result2.get(0).getId());
    }
}
