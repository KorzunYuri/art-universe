package yurykorzun.art.universe.music.quiz.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.entity.Artist;

import static org.junit.jupiter.api.Assertions.*;

class ArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_shouldPersistArtist_whenValidData() {
        // given
        Artist artist = new Artist();
        artist.setMasterId(1L);

        // when
        Artist savedArtist = artistRepository.save(artist);
        entityManager.flush();

        // then
        assertNotNull(savedArtist.getId());
        assertEquals(1L, savedArtist.getMasterId());
    }

    @Test
    void save_shouldThrowException_whenDuplicateMasterId() {
        // given
        Artist artist1 = new Artist();
        artist1.setMasterId(2L);
        artistRepository.save(artist1);
        entityManager.flush();

        Artist artist2 = new Artist();
        artist2.setMasterId(2L); // Same masterId as artist1

        // when & then
        assertThrows(Exception.class, () -> {
            artistRepository.save(artist2);
            entityManager.flush(); // Force the constraint violation to be detected
        });
    }

    @Test
    void save_shouldAllowDifferentMasterIds() {
        // given
        Artist artist1 = new Artist();
        artist1.setMasterId(3L);

        Artist artist2 = new Artist();
        artist2.setMasterId(4L);

        // when
        artistRepository.save(artist1);
        artistRepository.save(artist2);
        entityManager.flush();

        // then
        assertEquals(2, artistRepository.count());
    }
}
