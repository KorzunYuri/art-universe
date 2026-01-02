package yurykorzun.art.universe.music.data.master.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import yurykorzun.art.universe.music.data.master.common.archetypes.BaseMasterDataJpaTest;
import yurykorzun.art.universe.music.data.master.dto.binding.BoundEntityProjection;
import yurykorzun.art.universe.music.data.master.entity.Artist;
import yurykorzun.art.universe.music.data.master.entity.ArtistBinding;
import yurykorzun.art.universe.music.data.master.entity.DataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ArtistBindingRepositoryTest extends BaseMasterDataJpaTest {

    @Autowired
    private ArtistBindingRepository bindingRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    void shouldReturnBoundArtistsWithNames() {
        // given
        Artist masterArtist = em.persist(Artist.builder().name("Radiohead").build());
        em.flush();

        ArtistBinding binding = ArtistBinding.builder()
            .masterId(masterArtist.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();
        em.persist(binding);
        em.flush();

        // when
        List<BoundEntityProjection> results = bindingRepository.findBoundArtistsForDataSource(
            DataSource.LASTFM, List.of(123L)
        );

        // then
        assertThat(results).hasSize(1);
        BoundEntityProjection result = results.get(0);
        assertThat(result.getExternalId()).isEqualTo(123L);
        assertThat(result.getDataSource()).isEqualTo(DataSource.LASTFM);
        assertThat(result.getMasterId()).isEqualTo(masterArtist.getId());
        assertThat(result.getMasterName()).isEqualTo("Radiohead");
    }

    @Test
    void shouldHandleMultipleBindings() {
        // given
        Artist masterArtist1 = em.persist(Artist.builder().name("Radiohead").build());
        Artist masterArtist2 = em.persist(Artist.builder().name("Coldplay").build());
        em.flush();

        ArtistBinding binding1 = ArtistBinding.builder()
            .masterId(masterArtist1.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();

        ArtistBinding binding2 = ArtistBinding.builder()
            .masterId(masterArtist2.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(456L)
            .build();

        em.persist(binding1);
        em.persist(binding2);
        em.flush();

        // when
        List<BoundEntityProjection> results = bindingRepository.findBoundArtistsForDataSource(
            DataSource.LASTFM, List.of(123L, 456L)
        );

        // then
        assertThat(results).hasSize(2);
        assertThat(results).extracting(BoundEntityProjection::getExternalId)
            .containsExactlyInAnyOrder(123L, 456L);
        assertThat(results).extracting(BoundEntityProjection::getMasterName)
            .containsExactlyInAnyOrder("Radiohead", "Coldplay");
    }

    @Test
    void shouldReturnEmptyListWhenNoMatches() {
        // given
        Artist masterArtist = em.persist(Artist.builder().name("Radiohead").build());
        em.flush();

        ArtistBinding binding = ArtistBinding.builder()
            .masterId(masterArtist.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();
        em.persist(binding);
        em.flush();

        // when
        List<BoundEntityProjection> results = bindingRepository.findBoundArtistsForDataSource(
            DataSource.LASTFM, List.of(456L)
        );

        // then
        assertThat(results).isEmpty();
    }

    @Test
    void shouldHandleDifferentDataSources() {
        // given
        Artist masterArtist = em.persist(Artist.builder().name("Radiohead").build());
        em.flush();

        ArtistBinding lastfmBinding = ArtistBinding.builder()
            .masterId(masterArtist.getId())
            .dataSource(DataSource.LASTFM)
            .externalId(123L)
            .build();

        ArtistBinding spotifyBinding = ArtistBinding.builder()
            .masterId(masterArtist.getId())
            .dataSource(DataSource.SPOTIFY)
            .externalId(456L)
            .build();

        em.persist(lastfmBinding);
        em.persist(spotifyBinding);
        em.flush();

        // when
        List<BoundEntityProjection> lastfmResults = bindingRepository.findBoundArtistsForDataSource(
            DataSource.LASTFM, List.of(123L)
        );

        List<BoundEntityProjection> spotifyResults = bindingRepository.findBoundArtistsForDataSource(
            DataSource.SPOTIFY, List.of(456L)
        );

        // then
        assertThat(lastfmResults).hasSize(1);
        assertThat(spotifyResults).hasSize(1);
        assertThat(lastfmResults.get(0).getExternalId()).isEqualTo(123L);
        assertThat(spotifyResults.get(0).getExternalId()).isEqualTo(456L);
    }
}
