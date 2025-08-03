package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

@Component
@Slf4j
public class LastfmArtistTopAlbumsApiCallGenerator extends LastfmArtistApiCallGenerator {

    @Value("${lastfm.client.methods.artist.topAlbums.dueDurationDays}")
    private int dueDurationDays;

    protected LastfmArtistTopAlbumsApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService, entityService);
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_ALBUMS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }
}