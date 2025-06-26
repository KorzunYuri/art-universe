package yurykorzun.art.universe.music.data.approved.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;import yurykorzun.art.universe.music.data.approved.entity.Track;

@Repository
public interface TrackRepository extends JpaRepository<Track, Long> {
    
    Optional<Track> findByNameAndPrimaryArtistId(String name, Long primaryArtistId);}
