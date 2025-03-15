package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.repository;

import org.springframework.data.jpa.repository.JpaRepository;
        ;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeHistoryRecord;

public interface LastfmAttributeHistoryRecordRepository extends JpaRepository<LastfmAttributeHistoryRecord, Long> {
}
