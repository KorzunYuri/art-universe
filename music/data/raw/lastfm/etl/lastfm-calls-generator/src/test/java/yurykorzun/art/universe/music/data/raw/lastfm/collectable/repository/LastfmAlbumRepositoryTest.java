package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.common.DbConsistencyHelper;
import yurykorzun.art.universe.music.data.raw.lastfm.common.archetypes.JpaOnlyTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@Tag("integration")
@Import({
        DbConsistencyHelper.class,
})
class LastfmAlbumRepositoryTest extends JpaOnlyTest {

    @Autowired
    private LastfmAlbumRepository albumRepository;

    @Autowired
    private DbConsistencyHelper consistencyHelper;

    @Autowired
    private EntityManager entityManager;
    
    @BeforeEach
    void setUp() {
        consistencyHelper.cleanup();
    }
    
    @AfterEach
    void tearDown() {
        consistencyHelper.cleanup();
    }

    @Test
    void findAlbumsForGetInfo_shouldReturnAlbums() {
        // given
        consistencyHelper.createAndSaveAlbum();
        consistencyHelper.createAndSaveAlbum();

        // when
        List<LastfmAlbum> result = albumRepository.findAlbumsForGetInfo();

        // then
        assertNotNull(result);
    }
}
