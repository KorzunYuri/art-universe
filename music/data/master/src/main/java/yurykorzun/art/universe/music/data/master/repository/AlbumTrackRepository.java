package yurykorzun.art.universe.music.data.master.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.master.entity.AlbumTrack;

import java.util.Optional;

@Repository
public interface AlbumTrackRepository extends JpaRepository<AlbumTrack, Long> {

    Optional<AlbumTrack> findByAlbumIdAndTrackIdAndRelationTypeId(Long albumId, Long trackId, Long relationTypeId);
}
