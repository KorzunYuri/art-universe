package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.getinfo.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.service.LastfmArtistService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;

import java.util.List;

@Component
@Slf4j
public class LastfmArtistGetInfoApiCallGenerator extends LastfmArtistApiCallGenerator {

    private final LastfmArtistService artistService;

    @Value("${lastfm.client.methods.artist.getInfo.dueDurationDays}")
    private int dueDurationDays;

    public LastfmArtistGetInfoApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmArtistService artistService,
        LastfmDataSnapshotService snapshotService,
        LastfmEntityService entityService
    ) {
        super(apiCallService, snapshotService, entityService);

        this.artistService = artistService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_GET_INFO;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmArtist> selectEntitiesForApiCalls() {
        // note: deduplication is already applied on SQL level
        return artistService.findAllToGetInfoFor();
    }
}
