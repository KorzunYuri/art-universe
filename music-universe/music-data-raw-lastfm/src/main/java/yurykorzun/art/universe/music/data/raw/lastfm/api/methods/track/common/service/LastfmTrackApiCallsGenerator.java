package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;

import java.util.HashMap;
import java.util.Map;

public abstract class LastfmTrackApiCallsGenerator extends EntityScopedApiCallGenerator<LastfmTrack> {

    protected LastfmTrackApiCallsGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.TRACK;
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
