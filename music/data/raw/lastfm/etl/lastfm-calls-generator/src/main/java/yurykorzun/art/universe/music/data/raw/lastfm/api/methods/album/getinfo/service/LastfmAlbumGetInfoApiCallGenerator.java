package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.getinfo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.album.common.service.LastfmAlbumApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmAlbumService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;

import java.util.List;

@Component
@Slf4j
public class LastfmAlbumGetInfoApiCallGenerator extends LastfmAlbumApiCallGenerator {

    private final LastfmAlbumService albumService;

    @Value("${lastfm.client.methods.album.getInfo.dueDurationDays:28}")
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
