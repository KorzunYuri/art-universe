package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.time.LocalDate;

@Repository
public interface LastfmDataSnapshotRepository extends JpaRepository<LastfmDataSnapshot, Long> {

    @Modifying
    @Query(value = """
            UPDATE  data_snapshot
            SET     created_cnt = created_cnt + :count
            WHERE   id = :id
        """, nativeQuery = true)
    void incCreatedCountByNumber(@Param("id") long id, @Param("count") int count);

    default void incCreatedCount(long id) {
        incCreatedCountByNumber(id, 1);
    }

    @Modifying
    @Query(value = """
            UPDATE  data_snapshot
            SET     completed_cnt = completed_cnt + :count
            WHERE   id = :id
        """, nativeQuery = true)
    void incCompletedCountByNumber(@Param("id") long id, @Param("count") int count);

    default void incCompletedCount(long id) {
        incCompletedCountByNumber(id, 1);
    }

    @Modifying
    @Query(value = """
            UPDATE  data_snapshot
            SET     parsed_cnt = parsed_cnt + :count
            WHERE   id = :id
        """, nativeQuery = true)
    void incParsedCountByNumber(@Param("id") long id, @Param("count") int count);

    default void incParsedCount(long id) {
        incParsedCountByNumber(id, 1);
    }

    LastfmDataSnapshot findByApiCallTypeAndEntityTypeAndEntityIdAndDataDate(
        LastfmApiCallType apiCallType,
        LastfmEntityType entityType,
        Long entityId,
        LocalDate dataDate
    );

    default LastfmDataSnapshot findForApiCallTypeAndEntity(LastfmApiCallType apiCallType, BaseLastfmEntity entity) {
        return findByApiCallTypeAndEntityTypeAndEntityIdAndDataDate(
                apiCallType, entity.getType(), entity.getId(), LocalDate.now());
    }

    LastfmDataSnapshot findByApiCallTypeAndDataDate(LastfmApiCallType apiCallType, LocalDate dataDate);

    default LastfmDataSnapshot findForApiCallType(LastfmApiCallType apiCallType) {
        return findByApiCallTypeAndDataDate(
            apiCallType, LocalDate.now());
    }
}
