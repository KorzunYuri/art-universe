package yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto;

import org.junit.jupiter.api.Test;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.dto.LastfmTagResponseDto;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.common.EntityCreationHelper;

import static org.junit.jupiter.api.Assertions.*;

class LastfmTagResponseDtoTest {

    @Test
    void from_shouldCreateDtoWithAllFields() {
        // Given
        long id = 42L;
        String name = "Test Tag";
        String url = "https://example.com/tag";
        Integer usageCount = 1000;
        Integer usageUsersCount = 500;
        
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder
            .id(id)
            .name(name)
            .url(url)
            .usageCount(usageCount)
            .usageUsersCount(usageUsersCount)
            .approvalStatus(ApprovalStatus.APPROVED)
        );
        
        // When
        LastfmTagResponseDto dto = LastfmTagResponseDto.from(tag);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertEquals(url, dto.url());
        assertEquals(ApprovalStatus.APPROVED.getCode(), dto.approvalStatus());
        assertEquals(usageCount, dto.usageCount());
        assertEquals(usageUsersCount, dto.usageUsersCount());
    }
    
    @Test
    void from_shouldHandleNullFields() {
        // Given
        long id = 42L;
        String name = "Test Tag";
        
        LastfmTag tag = EntityCreationHelper.createTag(builder -> builder
            .id(id)
            .name(name)
            .url(null)
            .usageCount(null)
            .usageUsersCount(null)
            .approvalStatus(ApprovalStatus.PENDING)
        );
        
        // When
        LastfmTagResponseDto dto = LastfmTagResponseDto.from(tag);
        
        // Then
        assertEquals(id, dto.id());
        assertEquals(name, dto.name());
        assertNull(dto.url());
        assertEquals(ApprovalStatus.PENDING.getCode(), dto.approvalStatus());
        assertNull(dto.usageCount());
        assertNull(dto.usageUsersCount());
    }
    
    @Test
    void recordMethods_shouldWorkCorrectly() {
        // Given
        LastfmTagResponseDto dto1 = new LastfmTagResponseDto(
            1L, "Tag", "https://url.com", 2, 1000, 500);
        LastfmTagResponseDto dto2 = new LastfmTagResponseDto(
            1L, "Tag", "https://url.com", 2, 1000, 500);
        LastfmTagResponseDto dto3 = new LastfmTagResponseDto(
            2L, "Different", "https://other.com", 1, 2000, 1000);
        
        // Then
        assertEquals(dto1, dto2, "Equal DTOs should be equal");
        assertNotEquals(dto1, dto3, "Different DTOs should not be equal");
        assertEquals(dto1.hashCode(), dto2.hashCode(), "Equal DTOs should have same hash code");
        
        // Test toString contains all fields
        String dtoString = dto1.toString();
        assertTrue(dtoString.contains("id=1"));
        assertTrue(dtoString.contains("name=Tag"));
        assertTrue(dtoString.contains("url=https://url.com"));
        assertTrue(dtoString.contains("approvalStatus=2"));
        assertTrue(dtoString.contains("usageCount=1000"));
        assertTrue(dtoString.contains("usageUsersCount=500"));
    }
}
