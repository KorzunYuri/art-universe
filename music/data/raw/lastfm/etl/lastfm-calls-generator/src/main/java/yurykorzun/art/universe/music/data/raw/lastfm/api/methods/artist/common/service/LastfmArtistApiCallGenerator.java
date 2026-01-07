package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class LastfmArtistApiCallGenerator extends EntityScopedApiCallGenerator<LastfmArtist> {

    protected LastfmArtistApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService
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
    protected LastfmApiCallEntityQueryConfig getUnprocessedEntitiesQueryConfig() {
        Sort sort = Sort.by(Sort.Direction.DESC, "listenersCount", "isPrimary");
        return LastfmApiCallEntityQueryConfig.builder().sort(sort).build();
    }

    @Override
    protected boolean isValidForApiCall(LastfmArtist artist) {
        boolean isValid = StringUtils.isNotBlank(artist.getMbid()) || StringUtils.isNotBlank(artist.getName());

        if (!isValid) {
            log.warn("Artist {} is not valid for api call {} creation: missing both mbid & name",
                artist.getId(), getApiCallType());
        }

        return isValid;
    }

    @Nullable
    @Override
    protected String getApiCallUniqueKey(LastfmArtist entity) {
        if (entity.getMbid() != null) {
            return String.format("mbid-%s", entity.getMbid());
        } else if (entity.getName() != null) {
            return String.format("names-%s", entity.getName());
        }
        return null;
    }

    /**
     * <p>Determines whether the artist should be replaced with the new candidate based on priority:
     * <ol>
     *     <li>approval status: APPROVED > PENDING > other statuses</li>
     *     <li>popularity</li>
     *     <li>age (lesser id)</li>
     * </ol></p>
     */
    @Override
    protected boolean hasHigherPriority(LastfmArtist candidate, @Nullable LastfmArtist existing) {
        if (existing == null) {
            return true;
        }

        // Compare approval statuses
        int existingStatusPriority = getApprovalStatusPriority(existing.getApprovalStatus());
        int candidateStatusPriority = getApprovalStatusPriority(candidate.getApprovalStatus());
        
        if (candidateStatusPriority > existingStatusPriority) {
            return true;
        } else if (candidateStatusPriority < existingStatusPriority) {
            return false;
        }
        
        // Compare artist listeners count
        if (isHigherValue(candidate.getListenersCount(), existing.getListenersCount())) {
            return true;
        } else if (isHigherValue(existing.getListenersCount(), candidate.getListenersCount())) {
            return false;
        }
        
        // Compare artist play count
        if (isHigherValue(candidate.getPlayCount(), existing.getPlayCount())) {
            return true;
        } else if (isHigherValue(existing.getPlayCount(), candidate.getPlayCount())) {
            return false;
        }
        
        // If everything is equal - choose the older one
        return candidate.getId() < existing.getId();
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
