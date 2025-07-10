package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.dto.LastfmArtistResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTrackResponseDtoTest {

    @Test
    void from_shouldCreateDtoWithAllFields() {
        // Given
        long id = 42L;
        String name = "Test Track";
        String url = "https://example.com/track";
        String mbid = "track-mbid-123";
        Long playCount = 1000L;
        Integer listenersCount = 500;
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder
            .id(1L)
            .name("Test Artist")
            .url("https://example.com/artist")
        );
        
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .mbid(mbid)
            .playCount(playCount)
            .listenersCount(listenersCount)
            .approvalStatus(ApprovalStatus.APPROVED)
            .artist(artist)
        );
        
        // When
        LastfmTrackResponseDto dto = LastfmTrackResponseDto.from(track);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertEquals(mbid, dto.mbid());
        assertEquals(ApprovalStatus.APPROVED.getCode(), dto.approvalStatus());
        assertEquals(playCount, dto.playCount());
        assertEquals(listenersCount, dto.listenersCount());
        
        // Check artist reference
        assertNotNull(dto.artist());
        assertEquals(artist.getId(), dto.artist().id());
        assertEquals(artist.getName(), dto.artist().name());
        assertEquals(artist.getUrl(), dto.artist().url());
    }
    
    @Test
    void from_shouldHandleNullFields() {
        // Given
        long id = 42L;
        String name = "Test Track";
        String url = "https://example.com/track";
        
        LastfmTrack track = EntityCreationHelper.createTrack(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .mbid(null)
            .playCount(null)
            .listenersCount(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .artist(null)
        );
        
        // When
        LastfmTrackResponseDto dto = LastfmTrackResponseDto.from(track);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertNull(dto.mbid());
        assertEquals(ApprovalStatus.PENDING.getCode(), dto.approvalStatus());
        assertNull(dto.playCount());
        assertNull(dto.listenersCount());
        assertNull(dto.artist());
    }
    
    @Test
    void recordMethods_shouldWorkCorrectly() {
        // Given
        LastfmArtistResponseDto artistDto = new LastfmArtistResponseDto(
            1L, "Artist", "https://artist.com", "artist-mbid", 2, 1000L, 500);
            
        LastfmTrackResponseDto dto1 = new LastfmTrackResponseDto(
            1L, "Track", "https://track.com", "track-mbid", 2, 500, 1000L, artistDto);
            
        LastfmTrackResponseDto dto2 = new LastfmTrackResponseDto(
            1L, "Track", "https://track.com", "track-mbid", 2, 500, 1000L, artistDto);
            
        LastfmTrackResponseDto dto3 = new LastfmTrackResponseDto(
            2L, "Different", "https://other.com", "other-mbid", 1, 200, 400L, null);
        
        // Then
        assertEquals(dto1, dto2, "Equal DTOs should be equal");
        assertNotEquals(dto1, dto3, "Different DTOs should not be equal");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Equal DTOs should have same hash code");
        
        // Test toString contains all fields
        String dtoString = dto1.toString();
        assertTrue(dtoString.contains("id=1"));
        assertTrue(dtoString.contains("name=Track"));
        assertTrue(dtoString.contains("url=https://track.com"));
        assertTrue(dtoString.contains("mbid=track-mbid"));
        assertTrue(dtoString.contains("approvalStatus=2"));
        assertTrue(dtoString.contains("playCount=1000"));
        assertTrue(dtoString.contains("listenersCount=500"));
        assertTrue(dtoString.contains("artist="));
    }
}
