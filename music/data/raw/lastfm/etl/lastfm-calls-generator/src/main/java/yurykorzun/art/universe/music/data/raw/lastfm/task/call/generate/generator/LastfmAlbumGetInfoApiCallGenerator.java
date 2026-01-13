package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    @Value("${lastfm.tasks.calls-generate.due-duration-days.album-get-info}")
    private int dueDurationDays;

    public LastfmAlbumGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmApiCallEntityService entityService,
        LastfmAlbumService albumService
    ) {
        super(apiCallService, snapshotService, entityService);
        this.albumService = albumService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ALBUM_GET_INFO;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }
    
    @Override
    protected List<LastfmAlbum> selectEntitiesForApiCalls() {
        return albumService.findAlbumsForGetInfo();
    }
}
