package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;

@Repository
public interface LastfmAlbumRepository extends BaseLastfmAlbumRepository {

    @Modifying
    @Query("""
        UPDATE album a
        SET a.approvalStatus = :status,
            a.updatedAt = CURRENT_TIMESTAMP
        WHERE a.artist.id = :artistId
            AND a.approvalStatus = yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus.PENDING
    """)
    int updateAlbumStatusByArtistId(@Param("artistId") Long artistId, @Param("status") ApprovalStatus status);

}
