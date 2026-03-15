package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmArtistApiCallGenerator;

import java.util.List;

@Component
@Slf4j
public class LastfmArtistGetInfoApiCallGenerator extends LastfmArtistApiCallGenerator {

    private final LastfmArtistService artistService;
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmArtistGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmArtistService artistService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(apiCallService, snapshotService, entityService);
        this.artistService = artistService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_INFO;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_ARTIST_GET_INFO);
    }

    @Override
    protected List<LastfmArtist> selectEntitiesForApiCalls() {
        return artistService.findArtistsForGetInfo();
    }
}
