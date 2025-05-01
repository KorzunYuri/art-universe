package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service;

import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.HashMap;
import java.util.Map;

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
