package yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.EntityTextContent;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.List;

public interface TestEntityTextContentRepository extends JpaRepository<EntityTextContent, Long> {

    List<EntityTextContent> findByEntityTypeAndEntityId(LastfmEntityType entityType, long entityId);
}
