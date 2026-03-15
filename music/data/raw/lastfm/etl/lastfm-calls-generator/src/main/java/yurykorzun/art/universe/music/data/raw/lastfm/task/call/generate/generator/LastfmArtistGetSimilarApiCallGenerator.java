package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.integration.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmArtistApiCallGenerator;

import java.util.Map;

@Component
@Slf4j
public class LastfmArtistGetSimilarApiCallGenerator extends LastfmArtistApiCallGenerator {

    private final ConfigPropertyHolder configPropertyHolder;

    protected LastfmArtistGetSimilarApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_SIMILAR;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_ARTIST_GET_SIMILAR);
    }

    @Override
    protected Map<String, String> applyCustomApiCallParameters(Map<String, String> params) {
        params.put(LastfmApiConstants.PARAM_NAME_LIMIT, String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE));
        return params;
    }
}
