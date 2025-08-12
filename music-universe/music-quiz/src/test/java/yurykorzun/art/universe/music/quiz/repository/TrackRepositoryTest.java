package yurykorzun.art.universe.music.quiz.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.entity.Track;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
class TrackRepositoryTest extends JpaOnlyTest {

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void save_shouldPersistTrack_whenValidData() {
        // given
        Track track = new Track();
        track.setMasterId(1L);

        // when
        Track savedTrack = trackRepository.save(track);
        entityManager.flush();

        // then
        assertNotNull(savedTrack.getId());
        assertEquals(1L, savedTrack.getMasterId());
    }

    @Test
    void save_shouldThrowException_whenDuplicateMasterId() {
        // given
        Track track1 = new Track();
        track1.setMasterId(2L);
        trackRepository.save(track1);
        entityManager.flush();

        Track track2 = new Track();
        track2.setMasterId(2L); // Same masterId as track1

        // when & then
        assertThrows(Exception.class, () -> {
            trackRepository.save(track2);
            entityManager.flush(); // Force the constraint violation to be detected
        });
    }

    @Test
    void save_shouldAllowDifferentMasterIds() {
        // given
        Track track1 = new Track();
        track1.setMasterId(3L);

        Track track2 = new Track();
        track2.setMasterId(4L);

        // when
        trackRepository.save(track1);
        trackRepository.save(track2);
        entityManager.flush();

        // then
        assertEquals(2, trackRepository.count());
    }

    @Test
    void findByMasterId_shouldReturnTrack_whenExists() {
        // given
        Track track = new Track();
        track.setMasterId(5L);
        trackRepository.save(track);
        entityManager.flush();

        // when
        Optional<Track> result = trackRepository.findByMasterId(5L);

        // then
        assertTrue(result.isPresent());
        assertEquals(5L, result.get().getMasterId());
    }

    @Test
    void findByMasterId_shouldReturnEmpty_whenNotExists() {
        // when
        Optional<Track> result = trackRepository.findByMasterId(999L);

        // then
        assertFalse(result.isPresent());
    }

    @Test
    void findByMasterIdIn_shouldReturnMatchingTracks() {
        // given
        Track track1 = new Track();
        track1.setMasterId(10L);
        Track track2 = new Track();
        track2.setMasterId(20L);
        Track track3 = new Track();
        track3.setMasterId(30L);

        trackRepository.save(track1);
        trackRepository.save(track2);
        trackRepository.save(track3);
        entityManager.flush();

        // when
        List<Track> result = trackRepository.findByMasterIdIn(List.of(10L, 20L, 999L));

        // then
        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(t -> t.getMasterId().equals(10L)));
        assertTrue(result.stream().anyMatch(t -> t.getMasterId().equals(20L)));
        assertFalse(result.stream().anyMatch(t -> t.getMasterId().equals(30L)));
    }

    @Test
    void findByMasterIdIn_shouldReturnEmpty_whenNoMatches() {
        // when
        List<Track> result = trackRepository.findByMasterIdIn(List.of(999L, 888L));

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void deleteByMasterId_shouldRemoveTrack_whenExists() {
        // given
        Track track = new Track();
        track.setMasterId(100L);
        trackRepository.save(track);
        entityManager.flush();

        // when
        trackRepository.deleteByMasterId(100L);
        entityManager.flush();

        // then
        Optional<Track> result = trackRepository.findByMasterId(100L);
        assertFalse(result.isPresent());
    }

    @Test
    void deleteByMasterId_shouldNotThrow_whenNotExists() {
        // when & then
        assertDoesNotThrow(() -> {
            trackRepository.deleteByMasterId(999L);
            entityManager.flush();
        });
    }
}
