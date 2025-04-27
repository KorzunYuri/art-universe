package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.topalbums.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallsGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopAlbumsApiCallGenerator extends LastfmArtistApiCallsGenerator {

    private final LastfmEntityService entityService;

    @Value("${lastfm.client.methods.artist.topAlbums.dueDurationDays}")
    private int dueDurationDays;

    protected LastfmArtistTopAlbumsApiCallGenerator(
        LastfmApiCallService lastfmApiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(lastfmApiCallService, snapshotService);

        this.entityService = entityService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_ALBUMS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmArtist> selectEntitiesForApiCalls() {
        Sort sort = Sort.by(Sort.Direction.DESC, "listenersCount");
        LastfmEntityQueryConfig config = LastfmEntityQueryConfig.builder().sort(sort).build();
        return entityService.findAllUnprocessed(LastfmEntityType.ARTIST, LastfmApiCallType.ARTIST_TOP_ALBUMS, config);
    }
}