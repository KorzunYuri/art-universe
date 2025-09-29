package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.attribute;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeHistoryRecord;

public interface BaseLastfmAttributeHistoryRecordRepository extends JpaRepository<LastfmAttributeHistoryRecord, Long> {
}
