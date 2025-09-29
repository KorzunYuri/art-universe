package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@Import({
        DbConsistencyHelper.class,
})
class LastfmArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmArtistRepository artistRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    private LastfmArtist radiohead;
    private LastfmArtist radioMoscow;
    private LastfmArtist metallica;

    @BeforeEach
    void setup() {
        // Create test data
        radiohead = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Radiohead")
            .playCount(10000L)
            .listenersCount(5000)
            .approvalStatus(ApprovalStatus.APPROVED));
            
        radioMoscow = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Radio Moscow")
            .playCount(5000L)
            .listenersCount(2000)
            .approvalStatus(ApprovalStatus.PENDING));
            
        metallica = consistencyHelper.createAndSaveArtist(builder -> builder
            .name("Metallica")
            .playCount(20000L)
            .listenersCount(10000)
            .approvalStatus(ApprovalStatus.APPROVED));
    }
    
    @AfterEach
    void cleanup() {
        consistencyHelper.cleanup();
    }

    @Test
    void findAllToGetInfoFor_shouldReturnArtists() {

        // when
        List<LastfmArtist> result = artistRepository.findAllToGetInfoFor();

        // then
        assertNotNull(result);
    }
}

