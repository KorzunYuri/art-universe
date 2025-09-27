package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.service;

import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public abstract class LastfmAlbumApiCallGenerator extends EntityScopedApiCallGenerator<LastfmAlbum> {

    protected LastfmAlbumApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.ALBUM;
    }

    @Override
    protected boolean isValidForApiCall(LastfmAlbum entity) {
        // either album.mbid or artist.name + album.name must be present
        boolean isValid = StringUtils.isNotBlank(entity.getMbid())
            || (StringUtils.isNotBlank(entity.getName()) && artistIsValid(entity));

        if (!isValid) {
            log.warn("Album {} is not valid for api call {} creation: missing both mbid & artist", entity.getId(), getApiCallType());
        }

        return isValid;
    }

    private static boolean artistIsValid(LastfmAlbum entity) {
        return entity.getArtist() != null && StringUtils.isNotBlank(entity.getArtist().getName());
    }

    @Override
    protected @Nullable String getApiCallUniqueKey(LastfmAlbum entity) {
        if (StringUtils.isNotBlank(entity.getMbid())) {
            return String.format("mbid-%s", entity.getMbid());
        } else if (artistIsValid(entity)) {
            return String.format("names-%s-%s", entity.getArtist().getName(), entity.getName());
        }
        return null;
    }

    @Override
    protected boolean hasHigherPriority(LastfmAlbum candidate, @Nullable LastfmAlbum existing) {
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
        
        // Compare album listeners count
        if (isHigherValue(candidate.getListenersCount(), existing.getListenersCount())) {
            return true;
        } else if (isHigherValue(existing.getListenersCount(), candidate.getListenersCount())) {
            return false;
        }
        
        // Compare album play count
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

    private static Integer getArtistMetric(LastfmAlbum entity) {
        if (entity.getArtist() == null) {
            return null;
        }
        return entity.getArtist().getListenersCount();
    }


    @Override
    protected Map<String, String> getCommonApiCallParameters(LastfmAlbum album) {
        Map<String, String> params = new HashMap<>();

        // Either mbid (preferred) or album name + artist name must be provided
        if (album.getMbid() != null) {
            params.put(LastfmApiConstants.PARAM_NAME_MBID, album.getMbid());
        } else {
            params.put(LastfmApiConstants.PARAM_NAME_ALBUM, album.getName());
            // Artist is required when using album name
            if (album.getArtist() != null) {
                params.put(LastfmApiConstants.PARAM_NAME_ARTIST, album.getArtist().getName());
            }
        }

        return params;
    }
}
