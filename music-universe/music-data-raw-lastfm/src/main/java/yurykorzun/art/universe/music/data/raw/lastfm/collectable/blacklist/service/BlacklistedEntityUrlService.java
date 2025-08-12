package yurykorzun.art.universe.music.data.raw.lastfm.collectable.blacklist.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.blacklist.repository.BlacklistedEntityUrlRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.List;

@Service
@Slf4j
public class BlacklistedEntityUrlService {

    private final BlacklistedEntityUrlRepository blacklistRepository;

    public BlacklistedEntityUrlService(BlacklistedEntityUrlRepository blacklistRepository) {
        this.blacklistRepository = blacklistRepository;
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
}
