package yurykorzun.art.universe.music.data.master.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MasterBaseEntityTest {

    @Test
    void defaultValues_shouldBeUnknownAndNew() {
        Artist artist = Artist.builder().name("Test").build();

        assertEquals(Origin.UNKNOWN, artist.getOrigin());
        assertEquals(MasterApprovalStatus.NEW, artist.getApprovalStatus());
    }

    @Test
    void builder_shouldAllowOverridingOrigin() {
        Artist artist = Artist.builder()
            .name("Test")
            .origin(Origin.ETL)
            .build();

        assertEquals(Origin.ETL, artist.getOrigin());
    }

    @Test
    void builder_shouldAllowOverridingApprovalStatus() {
        Artist artist = Artist.builder()
            .name("Test")
            .approvalStatus(MasterApprovalStatus.PENDING)
            .build();

        assertEquals(MasterApprovalStatus.PENDING, artist.getApprovalStatus());
    }

    @Test
    void builder_shouldAllowOverridingBothFields() {
        Artist artist = Artist.builder()
            .name("Test")
            .origin(Origin.AI)
            .approvalStatus(MasterApprovalStatus.PENDING)
            .build();

        assertEquals(Origin.AI, artist.getOrigin());
        assertEquals(MasterApprovalStatus.PENDING, artist.getApprovalStatus());
    }

    @Test
    void approvalStatus_shouldBeMutable() {
        Artist artist = Artist.builder()
            .name("Test")
            .approvalStatus(MasterApprovalStatus.PENDING)
            .build();

        artist.setApprovalStatus(MasterApprovalStatus.APPROVED);

        assertEquals(MasterApprovalStatus.APPROVED, artist.getApprovalStatus());
    }
}
