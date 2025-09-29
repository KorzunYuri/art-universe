package yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.BlacklistedEntityUrl;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.List;

public interface BaseBlacklistedEntityUrlRepository extends JpaRepository<BlacklistedEntityUrl, Long> {

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
     * Inserts a multiple URLs into blacklist, ignoring duplicates
     */
    @Modifying
    @Query(value = """
        INSERT INTO blacklist_entity_url (entity_type, url)
        SELECT :entityTypeCode, unnest(CAST(:urls AS text[]))
        ON CONFLICT (entity_type, url) DO NOTHING
    """, nativeQuery = true)
    int insertIgnoreDuplicates(
        @Param("entityTypeCode") int entityTypeCode,
        @Param("urls") String[] urls
    );

    default int insertIgnoreDuplicates(LastfmEntityType entityType, List<String> urls) {
        return insertIgnoreDuplicates(entityType.getCode(), urls.toArray(new String[0]));
    }
}
