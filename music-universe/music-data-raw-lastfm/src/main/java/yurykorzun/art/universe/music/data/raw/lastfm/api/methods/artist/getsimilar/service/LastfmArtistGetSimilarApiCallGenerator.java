package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getsimilar.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallsGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Map;

@Component
@Slf4j
public class LastfmArtistGetSimilarApiCallGenerator extends LastfmArtistApiCallsGenerator {

    @Value("${lastfm.client.methods.artist.getSimilar.dueDurationDays}")
    private int dueDurationDays;

    protected LastfmArtistGetSimilarApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_SIMILAR;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected Map<String, String> applyCustomApiCallParameters(Map<String, String> params) {
        params.put(LastfmApiConstants.PARAM_NAME_LIMIT, String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE));
        return params;
    }
}
