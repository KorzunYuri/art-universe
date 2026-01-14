package yurykorzun.art.universe.music.data.raw.lastfm.domain.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.test.common.entity.EntityCreationHelper;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class LastfmAlbumResponseDtoTest {

    @Test
    void from_shouldCreateDtoWithAllFields() {
        // Given
        long id = 42L;
        String name = "Test Album";
        String url = "https://example.com/album";
        String mbid = "album-mbid-123";
        Long playCount = 5000L;
        Integer listenersCount = 1000;
        LocalDateTime publishTs = LocalDateTime.now();
        
        LastfmArtist artist = EntityCreationHelper.createArtist(builder -> builder
            .id(1L)
            .name("Test Artist")
            .url("https://example.com/artist")
        );
        
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .mbid(mbid)
            .playCount(playCount)
            .listenersCount(listenersCount)
            .publishTs(publishTs)
            .approvalStatus(ApprovalStatus.APPROVED)
            .artist(artist)
        );
        
        // When
        LastfmAlbumResponseDto dto = LastfmAlbumResponseDto.from(album);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertEquals(mbid, dto.mbid());
        assertEquals(ApprovalStatus.APPROVED.getCode(), dto.approvalStatus());
        assertEquals(playCount, dto.playCount());
        assertEquals(listenersCount, dto.listenersCount());
        assertEquals(publishTs, dto.publishTs());
        
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
        String name = "Test Album";
        String url = "https://example.com/album";
        
        LastfmAlbum album = EntityCreationHelper.createAlbum(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .mbid(null)
            .playCount(null)
            .listenersCount(null)
            .publishTs(null)
            .approvalStatus(ApprovalStatus.PENDING)
            .artist(null)
        );
        
        // When
        LastfmAlbumResponseDto dto = LastfmAlbumResponseDto.from(album);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertNull(dto.mbid());
        assertEquals(ApprovalStatus.PENDING.getCode(), dto.approvalStatus());
        assertNull(dto.playCount());
        assertNull(dto.listenersCount());
        assertNull(dto.publishTs());
        assertNull(dto.artist());
    }
    
    @Test
    void recordMethods_shouldWorkCorrectly() {
        // Given
        LastfmArtistResponseDto artistDto = new LastfmArtistResponseDto(
            1L, "Artist", "https://artist.com", "artist-mbid", 2, 1000L, 500);
            
        LastfmAlbumResponseDto dto1 = new LastfmAlbumResponseDto(
            1L, "Album", "https://album.com", "album-mbid", 2, 1000L, 500, null, artistDto);
            
        LastfmAlbumResponseDto dto2 = new LastfmAlbumResponseDto(
            1L, "Album", "https://album.com", "album-mbid", 2, 1000L, 500, null, artistDto);
            
        LastfmAlbumResponseDto dto3 = new LastfmAlbumResponseDto(
            2L, "Different", "https://other.com", "other-mbid", 1, 2000L, 1000, null, null);
        
        // Then
        assertEquals(dto1, dto2, "Equal DTOs should be equal");
        assertNotEquals(dto1, dto3, "Different DTOs should not be equal");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Equal DTOs should have same hash code");
        
        // Test toString contains all fields
        String dtoString = dto1.toString();
        assertTrue(dtoString.contains("id=1"));
        assertTrue(dtoString.contains("name=Album"));
        assertTrue(dtoString.contains("url=https://album.com"));
        assertTrue(dtoString.contains("mbid=album-mbid"));
        assertTrue(dtoString.contains("approvalStatus=2"));
        assertTrue(dtoString.contains("playCount=1000"));
        assertTrue(dtoString.contains("listenersCount=500"));
        assertTrue(dtoString.contains("artist="));
    }
}
