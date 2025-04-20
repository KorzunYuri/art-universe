package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptracks.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallsGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityQueryConfig;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class LastfmArtistTopTracksApiCallGenerator extends LastfmArtistApiCallsGenerator {

    private final LastfmEntityService entityService;
    private final LastfmDataSnapshotService dataSnapshotService;
    private final LastfmApiCallService apiCallService;

    @Value("${lastfm.client.methods.artist.topTracks.dueDurationDays}")
    private int dueDurationDays;

    public LastfmArtistTopTracksApiCallGenerator(
        LastfmEntityService entityService, LastfmDataSnapshotService dataSnapshotService,
        LastfmApiCallService apiCallService
    ) {
        super(apiCallService, dataSnapshotService);

        this.entityService = entityService;
        this.dataSnapshotService = dataSnapshotService;
        this.apiCallService = apiCallService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TRACKS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmArtist> selectEntitiesForApiCalls() {
        // apply order: popular first
        LastfmEntityQueryConfig config = LastfmEntityQueryConfig.builder()
                .sort(Sort.by(Sort.Direction.DESC, "listenersCount"))
            .build();

        return entityService.findAllUnprocessed(LastfmEntityType.ARTIST, getApiCallType(), config);
    }

    @Override
    protected Map<String, String> applyCustomApiCallParameters(Map<String, String> params) {
        // limit tracks to hibernate size for now, consider configuring this property later
        params.put(LastfmApiConstants.PARAM_NAME_LIMIT, String.valueOf(LastfmConstants.HIBERNATE_BATCH_SIZE));
        return params;
    }
}
