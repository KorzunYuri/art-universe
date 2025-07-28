package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.*;

public abstract class LastfmArtistApiCallsGenerator extends EntityScopedApiCallGenerator<LastfmArtist> {

    protected LastfmArtistApiCallsGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.ARTIST;
    }

    /**
     * Returns artists for API calls generation, following the most common logic -
     * return those not having API calls of corresponding type, ordered by popularity.
     */
    @Override
    protected LastfmEntityQueryConfig getUnprocessedEntitiesQueryConfig() {
        Sort sort = Sort.by(Sort.Direction.DESC, "listenersCount");
        return LastfmEntityQueryConfig.builder().sort(sort).build();
    }

    @Override
    protected List<LastfmArtist> selectEntitiesForApiCalls() {
        List<LastfmArtist> unprocessed = entityService.findAllUnprocessed(
            getScopeEntityType(), getApiCallType(), getUnprocessedEntitiesQueryConfig()
        );

        return deduplicateByMbid(unprocessed);
    }

    /**
     * Deduplicate artists by MBID based on approval status and popularity metrics.
     */
    protected List<LastfmArtist> deduplicateByMbid(List<LastfmArtist> artists) {
        Map<String, LastfmArtist> uniqueArtists = new LinkedHashMap<>();
        
        for (LastfmArtist artist : artists) {
            String key = artist.getMbid() != null ? artist.getMbid() : "id_" + artist.getId();
            
            if (!uniqueArtists.containsKey(key)) {
                uniqueArtists.put(key, artist);
            } else {
                // select artist with better metrics
                LastfmArtist existing = uniqueArtists.get(key);
                if (shouldReplaceArtist(existing, artist)) {
                    uniqueArtists.put(key, artist);
                }
            }
        }
        
        return new ArrayList<>(uniqueArtists.values());
    }

    /**
     * Определяет, следует ли заменить существующего артиста на кандидата.
     * Приоритет: APPROVED > PENDING > другие статусы, больше слушателей > меньше слушателей, меньший ID.
     */
    protected boolean shouldReplaceArtist(LastfmArtist existing, LastfmArtist candidate) {
        // compare approval statuses
        int existingStatusPriority = getApprovalStatusPriority(existing.getApprovalStatus());
        int candidateStatusPriority = getApprovalStatusPriority(candidate.getApprovalStatus());
        
        if (existingStatusPriority != candidateStatusPriority) {
            return candidateStatusPriority > existingStatusPriority;
        }
        
        // compare popularity metrics
        Integer existingListeners = existing.getListenersCount();
        Integer candidateListeners = candidate.getListenersCount();
        
        if (existingListeners == null && candidateListeners != null) return true;
        if (existingListeners != null && candidateListeners == null) return false;
        if (existingListeners != null && candidateListeners != null) {
            if (!existingListeners.equals(candidateListeners)) {
                return candidateListeners > existingListeners;
            }
        }
        
        // if everything is equal - choose the older one
        return candidate.getId() < existing.getId();
    }

    private int getApprovalStatusPriority(ApprovalStatus status) {
        return switch (status) {
            case APPROVED -> 3;
            case PENDING -> 2;
            case PRE_APPROVED -> 1;
            case DECLINED -> 0;
        };
    }

    @Override
    protected Map<String, String> getCommonApiCallParameters(LastfmArtist artist) {
        Map<String, String> params = new HashMap<>();

        // don't apply name autocorrection
        params.put(LastfmApiConstants.PARAM_NAME_AUTOCORRECT, "0");

        // either mbid (preferred) or name must be provided
        if (artist.getMbid() != null) {
            params.put(LastfmApiConstants.PARAM_NAME_MBID, artist.getMbid());
        } else {
            params.put(LastfmApiConstants.PARAM_NAME_ARTIST, artist.getName());
        }

        return params;
    }
}
