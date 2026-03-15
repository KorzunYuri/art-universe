package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmAlbumApiCallGenerator;

import java.util.List;

@Component
@Slf4j
public class LastfmAlbumGetInfoApiCallGenerator extends LastfmAlbumApiCallGenerator {

    private final LastfmAlbumService albumService;
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmAlbumGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService,
        LastfmAlbumService albumService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(apiCallService, snapshotService, entityService);
        this.albumService = albumService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ALBUM_GET_INFO;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_ALBUM_GET_INFO);
    }

    @Override
    protected List<LastfmAlbum> selectEntitiesForApiCalls() {
        return albumService.findAlbumsForGetInfo();
    }
}
