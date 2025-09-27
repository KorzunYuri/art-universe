package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.service;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTrack;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class LastfmTrackApiCallGenerator extends EntityScopedApiCallGenerator<LastfmTrack> {

    protected LastfmTrackApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.TRACK;
    }

    @Override
    protected boolean isValidForApiCall(LastfmTrack track) {
        // either track.mbid or track.name + artist.name must be present
        boolean isValid = StringUtils.isNotBlank(track.getMbid())
            || (StringUtils.isNotBlank(track.getName()) && artistIsValid(track));

        if (!isValid) {
            log.warn("Track {} is not valid for api call {} creation: missing both mbid & artist+name",
                track.getId(), getApiCallType());
        }

        return isValid;
    }

    @Override
    protected @Nullable String getApiCallUniqueKey(LastfmTrack entity) {
        if (StringUtils.isNotBlank(entity.getMbid())) {
            return String.format("mbid-%s", entity.getMbid());
        } else if (artistIsValid(entity)) {
            return String.format("names-%s-%s", entity.getArtist().getName(), entity.getName());
        }
        return null;
    }

    private static boolean artistIsValid(LastfmTrack entity) {
        return entity.getArtist() != null && StringUtils.isNotBlank(entity.getArtist().getName());
    }

    @Override
    protected boolean hasHigherPriority(LastfmTrack candidate, @Nullable LastfmTrack existing) {
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
        
        // Compare track listeners count
        if (isHigherValue(candidate.getListenersCount(), existing.getListenersCount())) {
            return true;
        } else if (isHigherValue(existing.getListenersCount(), candidate.getListenersCount())) {
            return false;
        }
        
        // Compare track play count
        if (isHigherValue(candidate.getPlayCount(), existing.getPlayCount())) {
            return true;
        } else if (isHigherValue(existing.getPlayCount(), candidate.getPlayCount())) {
            return false;
        }
        
        // Compare artist listeners count
        Integer candidateArtistMetric = getArtistMetric(candidate);
        Integer existingArtistMetric = getArtistMetric(existing);
        
        if (isHigherValue(candidateArtistMetric, existingArtistMetric)) {
            return true;
        } else if (isHigherValue(existingArtistMetric, candidateArtistMetric)) {
            return false;
        }
        
        // If everything is equal - choose the older one
        return candidate.getId() < existing.getId();
    }

    private static Integer getArtistMetric(LastfmTrack entity) {
        if (entity.getArtist() == null) {
            return null;
        }
        return entity.getArtist().getListenersCount();
    }

    @Override
    protected Map<String, String> getCommonApiCallParameters(LastfmTrack track) {
        Map<String, String> params = new HashMap<>();

        // Either mbid (preferred) or track name + artist name must be provided
        if (track.getMbid() != null) {
            params.put(LastfmApiConstants.PARAM_NAME_MBID, track.getMbid());
        } else {
            params.put(LastfmApiConstants.PARAM_NAME_TRACK, track.getName());
            // Artist is required when using track name
            if (track.getArtist() != null) {
                params.put(LastfmApiConstants.PARAM_NAME_ARTIST, track.getArtist().getName());
            }
        }

        return params;
    }
}
