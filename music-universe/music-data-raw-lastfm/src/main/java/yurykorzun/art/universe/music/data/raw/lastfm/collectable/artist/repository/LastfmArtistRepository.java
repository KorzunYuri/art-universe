package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface LastfmArtistRepository extends JpaRepository<LastfmArtist, Long> {

    Optional<LastfmArtist> findByName(String name);
    List<LastfmArtist> findAllByNameIn(Collection<String> strings);

}
