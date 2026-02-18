package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.relationship.LastfmAlbumTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.relationship.BaseLastfmAlbumTrackRepository;

import java.util.List;

public interface LastfmAlbumTrackRepository extends BaseLastfmAlbumTrackRepository {

    @Query("""
        SELECT          at
        FROM            album_track at
        JOIN FETCH      at.track t
        LEFT JOIN FETCH t.artist
        WHERE       at.album.id = :albumId
        ORDER BY    at.position ASC
    """)
    List<LastfmAlbumTrack> findByAlbumIdWithTracks(@Param("albumId") Long albumId);
}
