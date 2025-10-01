package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LastfmDataSnapshotRepository extends BaseLastfmDataSnapshotRepository {

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
            SET     created_cnt = created_cnt + :count
            WHERE   id IN (:ids)
        """, nativeQuery = true)
    void incCreatedCountByNumber(@Param("ids") List<Long> ids, @Param("count") int count);

    default void incCreatedCount(List<Long> ids) {
        incCreatedCountByNumber(ids, 1);
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
