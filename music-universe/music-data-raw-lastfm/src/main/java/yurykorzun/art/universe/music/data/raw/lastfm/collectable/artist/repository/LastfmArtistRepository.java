package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;

import java.util.Collection;
import java.util.List;

@Repository
public interface LastfmArtistRepository extends JpaRepository<LastfmArtist, Long> {

    List<LastfmArtist> findAllByNameIn(Collection<String> strings);

}
