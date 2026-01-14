package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;

@Repository
public interface LastfmTrackRepository extends BaseLastfmTrackRepository {

    @Modifying
    @Query("""
        UPDATE track t
        SET t.approvalStatus = :status,
            t.updatedAt = CURRENT_TIMESTAMP
        WHERE   t.artist.id = :artistId
            AND t.approvalStatus = yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus.PENDING
    """)
    int updateTrackStatusByArtistId(@Param("artistId") Long artistId, @Param("status") ApprovalStatus status);

    @Modifying
    @Query("""
        UPDATE track t
        SET t.approvalStatus = :status,
            t.updatedAt = CURRENT_TIMESTAMP
        WHERE t.id IN (
                SELECT  at.track.id
                FROM    album_track at
                WHERE   at.album.id = :albumId
            )
            AND t.approvalStatus = yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus.PENDING
    """)
    int updateTrackStatusByAlbumId(@Param("albumId") Long albumId, @Param("status") ApprovalStatus status);

}
