package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.getinfo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.track.common.service.LastfmTrackApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.service.LastfmTrackService;

import java.util.List;

@Component
@Slf4j
public class LastfmTrackGetInfoApiCallGenerator extends LastfmTrackApiCallGenerator {

    private final LastfmTrackService trackService;

    @Value("${lastfm.client.methods.track.getInfo.dueDurationDays}")
    private int dueDurationDays;

    @Value("${lastfm.threshold.track.listenersCount:1000}")
    private int batchSize;

    public LastfmTrackGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService,
        LastfmTrackService trackService
    ) {
        super(apiCallService, snapshotService, entityService);
        this.trackService = trackService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TRACK_GET_INFO;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmTrack> selectEntitiesForApiCalls() {
        return trackService.findTracksForGetInfo();
    }

}
