package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import static org.junit.jupiter.api.Assertions.*;

class LastfmArtistResponseDtoTest {

    @Test
    void from_shouldCreateDtoWithAllFields() {
        // Given
        long id = 42L;
        String name = "Test Artist";
        String url = "https://example.com/artist";
        String mbid = "test-mbid-123";
        Long playCount = 1000L;
        Integer listenersCount = 500;
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .mbid(mbid)
            .playCount(playCount)
            .listenersCount(listenersCount)
            .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // When
        LastfmArtistResponseDto dto = LastfmArtistResponseDto.from(artist);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertEquals(mbid, dto.mbid());
        assertEquals(ApprovalStatus.APPROVED.getCode(), dto.approvalStatus());
        assertEquals(playCount, dto.playCount());
        assertEquals(listenersCount, dto.listenersCount());
    }
    
    @Test
    void from_shouldHandleNullFields() {
        // Given
        long id = 42L;
        String name = "Test Artist";
        String url = "https://example.com/artist";
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .mbid(null)
            .playCount(null)
            .listenersCount(null)
            .approvalStatus(ApprovalStatus.PENDING)
        );
        
        // When
        LastfmArtistResponseDto dto = LastfmArtistResponseDto.from(artist);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertNull(dto.mbid());
        assertEquals(ApprovalStatus.PENDING.getCode(), dto.approvalStatus());
        assertNull(dto.playCount());
        assertNull(dto.listenersCount());
    }
    
    @Test
    void recordMethods_shouldWorkCorrectly() {
        // Given
        LastfmArtistResponseDto dto1 = new LastfmArtistResponseDto(
            1L, "Artist", "https://url.com", "mbid", 2, 1000L, 500);
        LastfmArtistResponseDto dto2 = new LastfmArtistResponseDto(
            1L, "Artist", "https://url.com", "mbid", 2, 1000L, 500);
        LastfmArtistResponseDto dto3 = new LastfmArtistResponseDto(
            2L, "Different", "https://other.com", "other", 1, 2000L, 1000);
        
        // Then
        assertEquals(dto1, dto2, "Equal DTOs should be equal");
        assertNotEquals(dto1, dto3, "Different DTOs should not be equal");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Equal DTOs should have same hash code");
        
        // Test toString contains all fields
        String dtoString = dto1.toString();
        assertTrue(dtoString.contains("id=1"));
        assertTrue(dtoString.contains("name=Artist"));
        assertTrue(dtoString.contains("url=https://url.com"));
        assertTrue(dtoString.contains("mbid=mbid"));
        assertTrue(dtoString.contains("approvalStatus=2"));
        assertTrue(dtoString.contains("playCount=1000"));
        assertTrue(dtoString.contains("listenersCount=500"));
    }
}
