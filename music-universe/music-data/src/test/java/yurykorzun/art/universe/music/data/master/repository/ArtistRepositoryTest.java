package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import yurykorzun.art.universe.music.data.master.common.archetypes.JpaOnlyTest;
import yurykorzun.art.universe.music.data.master.entity.Artist;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
    
    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatchingArtists() {
        // given
        Artist artist1 = Artist.builder().name("Radiohead").build();
        Artist artist2 = Artist.builder().name("Radio Moscow").build();
        Artist artist3 = Artist.builder().name("Muse").build();
        
        em.persist(artist1);
        em.persist(artist2);
        em.persist(artist3);
        em.flush();
        
        // when
        List<Artist> result = artistRepository.findByNameContainingIgnoreCase("radio", 10);
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Artist::getName)
            .containsExactlyInAnyOrder("Radiohead", "Radio Moscow");
    }
    
    @Test
    void findByNameContainingIgnoreCase_shouldBeCaseInsensitive() {
        // given
        Artist artist1 = Artist.builder().name("Radiohead").build();
        Artist artist2 = Artist.builder().name("RADIO Moscow").build();
        Artist artist3 = Artist.builder().name("Muse").build();
        
        em.persist(artist1);
        em.persist(artist2);
        em.persist(artist3);
        em.flush();
        
        // when
        List<Artist> result = artistRepository.findByNameContainingIgnoreCase("rAdIo", 10);
        
        // then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Artist::getName)
            .containsExactlyInAnyOrder("Radiohead", "RADIO Moscow");
    }
    
    @Test
    void findByNameContainingIgnoreCase_shouldRespectLimit() {
        // given
        for (int i = 1; i <= 10; i++) {
            Artist artist = Artist.builder().name("Band " + i).build();
            em.persist(artist);
        }
        em.flush();
        
        // when
        List<Artist> result = artistRepository.findByNameContainingIgnoreCase("Band", 5);
        
        // then
        assertThat(result).hasSize(5);
    }
    
    @Test
    void findByNameContainingIgnoreCase_shouldReturnSortedResults() {
        // given
        Artist artist1 = Artist.builder().name("Band C").build();
        Artist artist2 = Artist.builder().name("Band A").build();
        Artist artist3 = Artist.builder().name("Band B").build();
        
        // Save in non-alphabetical order
        em.persist(artist1);
        em.persist(artist2);
        em.persist(artist3);
        em.flush();
        
        // when
        List<Artist> result = artistRepository.findByNameContainingIgnoreCase("Band", 10);
        
        // then
        assertThat(result).hasSize(3);
        // Should be sorted alphabetically
        assertThat(result.get(0).getName()).isEqualTo("Band A");
        assertThat(result.get(1).getName()).isEqualTo("Band B");
        assertThat(result.get(2).getName()).isEqualTo("Band C");
    }
}
