package yurykorzun.art.universe.music.quiz.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.entity.Artist;

import java.util.List;
import java.util.Optional;

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

    @Test
    void findByMasterId_shouldReturnArtist_whenExists() {
        // given
        Artist artist = new Artist();
        artist.setMasterId(5L);
        artistRepository.save(artist);
        entityManager.flush();

        // when
        Optional<Artist> result = artistRepository.findByMasterId(5L);

        // then
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getMasterId());
    }

    @Test
    void findByMasterId_shouldReturnEmpty_whenNotExists() {
        // when
        Optional<Artist> result = artistRepository.findByMasterId(999L);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMasterIdIn_shouldReturnMatchingArtists() {
        // given
        Artist artist1 = new Artist();
        artist1.setMasterId(10L);
        Artist artist2 = new Artist();
        artist2.setMasterId(20L);
        Artist artist3 = new Artist();
        artist3.setMasterId(30L);

        artistRepository.save(artist1);
        artistRepository.save(artist2);
        artistRepository.save(artist3);
        entityManager.flush();

        // when
        List<Artist> result = artistRepository.findByMasterIdIn(List.of(10L, 20L, 999L));

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(a -> a.getMasterId().equals(10L)));
        assertTrue(result.stream().anyMatch(a -> a.getMasterId().equals(20L)));
        assertFalse(result.stream().anyMatch(a -> a.getMasterId().equals(30L)));
    }

    @Test
    void findByMasterIdIn_shouldReturnEmpty_whenNoMatches() {
        // when
        List<Artist> result = artistRepository.findByMasterIdIn(List.of(999L, 888L));

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByMasterId_shouldRemoveArtist_whenExists() {
        // given
        Artist artist = new Artist();
        artist.setMasterId(100L);
        artistRepository.save(artist);
        entityManager.flush();

        // when
        artistRepository.deleteByMasterId(100L);
        entityManager.flush();

        // then
        Optional<Artist> result = artistRepository.findByMasterId(100L);
        assertFalse(result.isPresent());
    }

    @Test
    void deleteByMasterId_shouldNotThrow_whenNotExists() {
        // when & then
        assertDoesNotThrow(() -> {
            artistRepository.deleteByMasterId(999L);
            entityManager.flush();
        });
    }
}
