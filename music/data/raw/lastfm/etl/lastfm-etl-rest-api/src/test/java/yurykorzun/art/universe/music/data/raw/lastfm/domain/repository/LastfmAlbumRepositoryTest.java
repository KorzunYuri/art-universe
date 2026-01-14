package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.test.archetypes.LastfmJpaTestHelper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LastfmAlbumRepositoryTest extends LastfmJpaTestHelper {

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void updateAlbumStatusByArtistId_shouldUpdatePendingAlbumsOnly() {
        // Given
        LastfmArtist artist = consistencyHelper.createAndSaveArtist();
        
        LastfmAlbum pendingAlbum1 = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Pending Album 1")
                   .url("http://test1.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmAlbum pendingAlbum2 = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Pending Album 2")
                   .url("http://test2.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.PENDING));
                   
        LastfmAlbum approvedAlbum = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Approved Album")
                   .url("http://test3.com")
                   .artist(artist)
                   .approvalStatus(ApprovalStatus.APPROVED));
                   
        // Different artist's album
        LastfmArtist otherArtist = consistencyHelper.createAndSaveArtist();
        LastfmAlbum otherArtistAlbum = consistencyHelper.createAndSaveAlbum(builder -> 
            builder.name("Other Artist Album")
                   .url("http://test4.com")
                   .artist(otherArtist)
                   .approvalStatus(ApprovalStatus.PENDING));

        // make sure changes have been applied
        entityManager.flush();

        // When
        int updatedCount = albumRepository.updateAlbumStatusByArtistId(artist.getId(), ApprovalStatus.DECLINED);

        // Clear entity manager cache to get fresh data from DB
        entityManager.flush();
        entityManager.clear();

        // Then
        assertEquals(2, updatedCount);
        
        // Verify status changes
        LastfmAlbum updatedAlbum1 = albumRepository.findById(pendingAlbum1.getId()).orElseThrow();
        LastfmAlbum updatedAlbum2 = albumRepository.findById(pendingAlbum2.getId()).orElseThrow();
        LastfmAlbum unchangedApproved = albumRepository.findById(approvedAlbum.getId()).orElseThrow();
        LastfmAlbum unchangedOther = albumRepository.findById(otherArtistAlbum.getId()).orElseThrow();
        
        assertEquals(ApprovalStatus.DECLINED, updatedAlbum1.getApprovalStatus());
        assertEquals(ApprovalStatus.DECLINED, updatedAlbum2.getApprovalStatus());
        assertEquals(ApprovalStatus.APPROVED, unchangedApproved.getApprovalStatus());
        assertEquals(ApprovalStatus.PENDING, unchangedOther.getApprovalStatus());
    }
}
