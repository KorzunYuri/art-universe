package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.List;

@Repository
public interface LastfmTrackRepository extends JpaRepository<LastfmTrack, Long> {

    List<LastfmTrack> findAllByUrlIn(List<String> urls);

}
