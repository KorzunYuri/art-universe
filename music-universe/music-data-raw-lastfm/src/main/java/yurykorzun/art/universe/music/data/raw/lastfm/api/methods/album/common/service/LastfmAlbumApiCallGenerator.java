package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.HashMap;
import java.util.Map;

public abstract class LastfmAlbumApiCallGenerator extends EntityScopedApiCallGenerator<LastfmAlbum> {

    protected LastfmAlbumApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.ALBUM;
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
