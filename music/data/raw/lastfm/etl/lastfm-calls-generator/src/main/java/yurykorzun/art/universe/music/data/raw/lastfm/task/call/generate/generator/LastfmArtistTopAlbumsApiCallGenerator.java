package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmArtistApiCallGenerator;

@Component
@Slf4j
public class LastfmArtistTopAlbumsApiCallGenerator extends LastfmArtistApiCallGenerator {

    private final ConfigPropertyHolder configPropertyHolder;

    protected LastfmArtistTopAlbumsApiCallGenerator(
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
        return LastfmApiCallType.ARTIST_TOP_ALBUMS;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_ARTIST_TOP_ALBUMS);
    }
}
