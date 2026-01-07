package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;

import java.util.List;

@Repository
public interface LastfmAlbumRepository extends BaseLastfmAlbumRepository {

    @Query(value = """
        SELECT  a
        FROM    album a
        LEFT JOIN FETCH a.artist
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND (:artistId              IS NULL     OR a.artist.id = :artistId)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM album_tag at 
                WHERE at.album.id = a.id AND at.tag.id = :tagId
            ))
        """)
    Page<LastfmAlbum> findAlbumsWithoutApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("artistId")            Long artistId,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    @Query(value = """
        SELECT  a
        FROM    album a
        LEFT JOIN FETCH a.artist
        WHERE   1=1
            AND ((LOWER(a.name)  LIKE LOWER(CONCAT('%', :search, '%'))) OR :search  IS NULL)
            AND (:minPlayCount          IS NULL     OR a.playCount >= :minPlayCount)
            AND (:minListenersCount     IS NULL     OR a.listenersCount >= :minListenersCount)
            AND (:artistId              IS NULL     OR a.artist.id = :artistId)
            AND a.approvalStatus IN (:approvalStatuses)
            AND (:tagId IS NULL OR EXISTS (
                SELECT 1 FROM album_tag at 
                WHERE at.album.id = a.id AND at.tag.id = :tagId
            ))
        """)
    Page<LastfmAlbum> findAlbumsWithApprovalStatus(
        @Nullable @Param("search")              String search,
        @Nullable @Param("minPlayCount")        Long minPlayCount,
        @Nullable @Param("minListenersCount")   Long minListenersCount,
        @Nullable @Param("artistId")            Long artistId,
        @Param("approvalStatuses")              List<ApprovalStatus> approvalStatuses,
        @Nullable @Param("tagId")               Long tagId,
        Pageable pageable);

    /**
     * A wrapper for findAlbums for correct collection parameters resolution.
     * This implementation avoids Hibernate bugs with:
     * 1. Null String parameters being recognized as bytea
     * 2. Empty collections handling
     * 3. Ensures null values are sorted last for numeric fields
     */
    default Page<LastfmAlbum> findAlbums(
        String search,
        Long minPlayCount,
        Long minListenersCount,
        Long artistId,
        List<ApprovalStatus> approvalStatuses,
        Long tagId,
        Pageable pageable
    ) {
        if (approvalStatuses == null || approvalStatuses.isEmpty()) {
            return findAlbumsWithoutApprovalStatus(search, minPlayCount, minListenersCount, artistId, tagId, pageable);
        } else {
            return findAlbumsWithApprovalStatus(search, minPlayCount, minListenersCount, artistId, approvalStatuses, tagId, pageable);
        }
    }
}
