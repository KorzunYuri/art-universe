package yurykorzun.art.universe.music.data.raw.lastfm.collectable.service;

import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.repository.BlacklistedEntityUrlRepository;

import java.util.List;

@Service
@Slf4j
public class BlacklistedEntityUrlService {

    private final BlacklistedEntityUrlRepository blacklistRepository;
    private final EntityManager entityManager;

    public BlacklistedEntityUrlService(
        BlacklistedEntityUrlRepository blacklistRepository,
        EntityManager entityManager
    ) {
        this.blacklistRepository = blacklistRepository;
        this.entityManager = entityManager;
    }

    /**
     * Checks if a URL is blacklisted for the given entity type
     */
    public boolean isBlacklisted(LastfmEntityType entityType, String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }
        return blacklistRepository.existsByEntityTypeAndUrl(entityType, url);
    }

    /**
     * Returns list of blacklisted URLs from the provided list for the given entity type
     */
    public List<String> getBlacklisted(LastfmEntityType entityType, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }
        
        // Filter out null and empty URLs
        List<String> validUrls = urls.stream()
            .filter(url -> url != null && !url.trim().isEmpty())
            .distinct()
            .toList();
            
        if (validUrls.isEmpty()) {
            return List.of();
        }

        List<String> blacklisted = blacklistRepository.findBlacklistedUrls(entityType, validUrls);
        
        if (!blacklisted.isEmpty()) {
            log.debug("Found {} blacklisted {} URLs out of {}", 
                blacklisted.size(), entityType, validUrls.size());
        }
        
        return blacklisted;
    }

    /**
     * Adds a single URL to the blacklist for the given entity type
     * Uses ON CONFLICT DO NOTHING to handle duplicates
     */
    @Transactional
    public void addToBlacklist(LastfmEntityType entityType, String url) {
        if (url == null || url.trim().isEmpty()) {
            log.warn("Cannot add null or empty URL to blacklist for entity type: {}", entityType);
            return;
        }

        int inserted = blacklistRepository.insertIgnoreDuplicate(entityType, url);
        
        if (inserted > 0) {
            log.info("Added {} URL to blacklist: {}", entityType, url);
        } else {
            log.debug("URL already in blacklist for {}: {}", entityType, url);
        }
    }

    /**
     * Adds multiple URLs to the blacklist for the given entity type
     * Uses ON CONFLICT DO NOTHING to handle duplicates
     */
    @Transactional
    public void addToBlacklist(LastfmEntityType entityType, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            log.warn("Cannot add null or empty URL list to blacklist for entity type: {}", entityType);
            return;
        }

        // Filter out null and empty URLs
        List<String> validUrls = urls.stream()
            .filter(url -> url != null && !url.trim().isEmpty())
            .distinct()
            .toList();
            
        if (validUrls.isEmpty()) {
            log.warn("No valid URLs to add to blacklist for entity type: {}", entityType);
            return;
        }

        int inserted = blacklistRepository.insertIgnoreDuplicates(entityType, validUrls);
        
        log.info("Added {} new {} URLs to blacklist out of {} provided", 
            inserted, entityType, validUrls.size());
    }

    /**
     * Return list of URLs that should not be blacklisted due to two reasons:
     * <ul>
     *     <li>They are already in blacklist</li>
     *     <li>They are (pre)approved entities (approval_status = 2 or 4)</li>
     * </ul>
     */
    public List<String> findAllNonBlackListable(LastfmEntityType entityType, List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }

        // Filter out null and empty URLs
        List<String> validUrls = urls.stream()
            .filter(url -> url != null && !url.trim().isEmpty())
            .distinct()
            .toList();
            
        if (validUrls.isEmpty()) {
            return List.of();
        }

        String tableName = entityType.getName().toLowerCase();

        // Query to find URLs that should NOT be blacklisted:
        // 1. URLs of approved/pre-approved entities (approval_status = 2 or 4)
        // 2. URLs already in blacklist
        String sql = """
            SELECT DISTINCT url_to_exclude
            FROM (
                -- URLs of approved/pre-approved entities
                SELECT  e.url as url_to_exclude
                FROM    %s e
                WHERE   e.url IN :urls
                  AND   e.approval_status IN (2, 4)  -- APPROVED = 2, PRE_APPROVED = 4
                
                UNION
                
                -- URLs already in blacklist
                SELECT  b.url as url_to_exclude
                FROM    blacklist_entity_url b
                WHERE   b.entity_type = :entityType
                  AND   b.url IN :urls
            ) combined_exclusions
        """.formatted(tableName);

        @SuppressWarnings("unchecked")
        List<String> result = entityManager.createNativeQuery(sql)
            .setParameter("urls", validUrls)
            .setParameter("entityType", entityType.getCode())
            .getResultList();

        if (!result.isEmpty()) {
            log.debug("Found {} non-blacklistable {} URLs out of {} candidates", 
                result.size(), entityType, validUrls.size());
        }

        return result;
    }
}
