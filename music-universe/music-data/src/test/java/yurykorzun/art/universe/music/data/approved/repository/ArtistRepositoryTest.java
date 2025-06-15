package yurykorzun.art.universe.music.data.approved.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import yurykorzun.art.universe.music.data.approved.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.approved.entity.Artist;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("integration")
class ArtistRepositoryTest extends JpaOnlyTest {

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldSaveAndFindArtist() {
        // given
        Artist artist = Artist.builder()
            .name("Radiohead")
            .build();

        // when
        Artist saved = artistRepository.save(artist);
        em.flush();
        em.clear();

        // then
        Artist found = artistRepository.findById(saved.getId()).orElse(null);
        assertNotNull(found);
        assertThat(found.getName()).isEqualTo("Radiohead");
    }

    @Test
    void shouldFindArtistByName() {
        // given
        Artist artist = Artist.builder()
            .name("Radiohead")
            .build();
        em.persist(artist);
        em.flush();

        // when
        Optional<Artist> found = artistRepository.findByName("Radiohead");

        // then
        assertTrue(found.isPresent());
        assertThat(found.get().getName()).isEqualTo("Radiohead");
    }

    @Test
    void shouldUpdateArtist() {
        // given
        Artist artist = Artist.builder()
            .name("Radiohead")
            .build();
        em.persist(artist);
        em.flush();
        em.clear();

        // when
        Artist found = artistRepository.findById(artist.getId()).orElse(null);
        assertNotNull(found);
        found.setName("Radiohead (updated)");
        artistRepository.save(found);
        em.flush();
        em.clear();

        // then
        Artist updated = artistRepository.findById(artist.getId()).orElse(null);
        assertNotNull(updated);
        assertThat(updated.getName()).isEqualTo("Radiohead (updated)");
    }

    @Test
    void shouldDeleteArtist() {
        // given
        Artist artist = Artist.builder()
            .name("Radiohead")
            .build();
        em.persist(artist);
        em.flush();
        em.clear();

        // when
        artistRepository.deleteById(artist.getId());
        em.flush();

        // then
        assertThat(artistRepository.findById(artist.getId())).isEmpty();
    }
}
