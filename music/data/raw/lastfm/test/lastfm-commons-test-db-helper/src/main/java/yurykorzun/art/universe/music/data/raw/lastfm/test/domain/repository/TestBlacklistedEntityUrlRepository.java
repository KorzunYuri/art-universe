package yurykorzun.art.universe.music.data.raw.lastfm.test.domain.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.repository.BaseBlacklistedEntityUrlRepository;

public interface TestBlacklistedEntityUrlRepository extends BaseBlacklistedEntityUrlRepository {

    /**
     * Inserts a single URL into blacklist, ignoring duplicates
     */
    @Modifying
    @Query(value = """
        INSERT INTO blacklist_entity_url (entity_type, url)
        VALUES (:entityTypeCode, :url)
        ON CONFLICT (entity_type, url) DO NOTHING
    """, nativeQuery = true)
    int insertIgnoreDuplicate(
        @Param("entityTypeCode") int entityTypeCode,
        @Param("url") String url
    );

    default int insertIgnoreDuplicate(LastfmEntityType entityType, String url) {
        return  insertIgnoreDuplicate(entityType.getCode(), url);
    }

    /**
     * Checks if an entity is in the blacklist
     */
    boolean existsByEntityTypeAndUrl(LastfmEntityType entityType, String url);

}
