package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service;

import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.EntityScopedApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.HashMap;
import java.util.Map;

public abstract class LastfmTagApiCallGenerator extends EntityScopedApiCallGenerator<LastfmTag> {

    protected LastfmTagApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    protected LastfmEntityType getScopeEntityType() {
        return LastfmEntityType.TAG;
    }

    @Override
    protected LastfmEntityQueryConfig getUnprocessedEntitiesQueryConfig() {
        return LastfmEntityQueryConfig.builder()
                .sort(Sort.by(Sort.Direction.DESC, "usageCount"))
            .build();
    }

    @Override
    protected Map<String, String> getCommonApiCallParameters(LastfmTag tag) {
        Map<String, String> params = new HashMap<>();

        params.put(LastfmApiConstants.PARAM_NAME_TAG, tag.getName());

        return params;
    }
}
