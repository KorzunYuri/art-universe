package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    @Value("${lastfm.tasks.calls-generate.due-duration-days.artist-get-similar}")
    private int dueDurationDays;

    protected LastfmArtistGetSimilarApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService
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
