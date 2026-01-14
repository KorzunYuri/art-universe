package yurykorzun.art.universe.music.data.raw.lastfm.etl.repository;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.BlacklistedEntityUrl;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

import java.util.List;

public interface BlacklistedEntityUrlRepository extends BaseBlacklistedEntityUrlRepository {

    /**
     * Checks if an entity is in the blacklist
     */
    boolean existsByEntityTypeAndUrl(LastfmEntityType entityType, String url);

    /**
     * Finds all blacklist entries for the specified entity type and URL list
     */
    List<BlacklistedEntityUrl> findByEntityTypeAndUrlIn(LastfmEntityType entityType, List<String> urls);

    /**
     * Finds all blacklisted urls for the specified entity type and URL list
     */
    @Query("""
        SELECT  bl.url
        FROM    blacklist_entity_url bl
        WHERE   bl.entityType = :entityType
           AND  bl.url in :urls
    """)
    List<String> findBlacklistedUrls(
        @Param("entityType") LastfmEntityType entityType,
        @Param("urls") List<String> urls
    );

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
