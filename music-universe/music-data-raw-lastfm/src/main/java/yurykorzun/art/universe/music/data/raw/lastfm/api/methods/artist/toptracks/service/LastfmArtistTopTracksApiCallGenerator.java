package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.Map;

@Component
@Slf4j
public class LastfmArtistTopTracksApiCallGenerator extends LastfmArtistApiCallGenerator {

    @Value("${lastfm.client.methods.artist.topTracks.dueDurationDays}")
    private int dueDurationDays;

    public LastfmArtistTopTracksApiCallGenerator(
        LastfmDataSnapshotService dataSnapshotService,
        LastfmApiCallService apiCallService,
        LastfmEntityService entityService
    ) {
        super(apiCallService, dataSnapshotService, entityService);
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TRACKS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected Map<String, String> applyCustomApiCallParameters(Map<String, String> params) {
        // TODO limit tracks to hibernate batch size for now, consider configuring this property later
        params.put(LastfmApiConstants.PARAM_NAME_LIMIT, String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE));
        return params;
    }
}
