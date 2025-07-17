package yurykorzun.art.universe.music.quiz.repository;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import yurykorzun.art.universe.music.quiz.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.quiz.entity.Track;

import static org.junit.jupiter.api.Assertions.*;

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
}
