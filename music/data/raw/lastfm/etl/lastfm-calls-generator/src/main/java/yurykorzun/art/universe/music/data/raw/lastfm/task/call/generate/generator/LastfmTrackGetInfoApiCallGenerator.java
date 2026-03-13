package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
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
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmTrackGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService,
        LastfmTrackService trackService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(apiCallService, snapshotService, entityService);
        this.trackService = trackService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TRACK_GET_INFO;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TRACK_GET_INFO);
    }

    @Override
    protected List<LastfmTrack> selectEntitiesForApiCalls() {
        return trackService.findTracksForGetInfo();
    }
}
