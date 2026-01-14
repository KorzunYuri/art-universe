package yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.attribute;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttributeHistoryRecord;

@NoRepositoryBean
public interface BaseLastfmAttributeHistoryRecordRepository extends JpaRepository<LastfmAttributeHistoryRecord, Long> {
}
