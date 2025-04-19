package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.HashMap;
import java.util.Map;

public abstract class LastfmTagApiCallGenerator extends EntityScopedApiCallGenerator<LastfmTag> {

    protected LastfmTagApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService
    ) {
        super(lastfmApiCallService, snapshotService);
    }

    @Override
    protected Map<String, String> getCommonApiCallParameters(LastfmTag tag) {
        Map<String, String> params = new HashMap<>();

        params.put(LastfmApiConstants.PARAM_NAME_TAG, tag.getName());

        return params;
    }
}
