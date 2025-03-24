package yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmDataSnapshot;

@Repository
public interface LastfmDataSnapshotRepository extends JpaRepository<LastfmDataSnapshot, Long> {

    @Modifying
    @Query(value =
            "UPDATE data_snapshot " +
            "SET completed_cnt = completed_cnt + 1 " +
            "WHERE  id = :id",
        nativeQuery = true)
    void incCompletedCount(@Param("id") long id);

    @Modifying
    @Query(value =
            "UPDATE data_snapshot " +
            "SET parsed_cnt = parsed_cnt + 1 " +
            "WHERE  id = :id",
            nativeQuery = true)
    void incParsedCount(@Param("id") long id);
}
