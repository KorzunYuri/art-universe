package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;

import java.util.HashMap;
import java.util.Map;

public abstract class LastfmArtistApiCallsGenerator extends EntityScopedApiCallGenerator<LastfmArtist> {

    protected LastfmArtistApiCallsGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService
    ) {
        super(lastfmApiCallService, snapshotService);
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
