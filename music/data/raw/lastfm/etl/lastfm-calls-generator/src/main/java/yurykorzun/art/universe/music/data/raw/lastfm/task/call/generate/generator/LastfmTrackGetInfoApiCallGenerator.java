package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmTrackService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmTrackApiCallGenerator;

import java.util.List;

@Component
@Slf4j
public class LastfmTrackGetInfoApiCallGenerator extends LastfmTrackApiCallGenerator {

    private final LastfmTrackService trackService;

    @Value("${lastfm.tasks.calls-generate.due-duration-days.track-get-info}")
    private int dueDurationDays;

    @Value("${lastfm.threshold.track.listenersCount:1000}")
    private int batchSize;

    public LastfmTrackGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService,
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
