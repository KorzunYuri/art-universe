package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.toptags.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.artist.common.service.LastfmArtistApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service.SnapshotAttributeInfo;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeSnapshotService;

import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopTagsApiCallGenerator extends LastfmArtistApiCallGenerator {

    private static final List<SnapshotAttributeInfo> snapshotAttributes = List.of(
        new SnapshotAttributeInfo(LastfmAttribute.RANK, LastfmEntityType.TAG)
    );

    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.tasks.calls-generate.due-duration-days.artist-top-tags}")
    private int dueDurationDays;

    public LastfmArtistTopTagsApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmApiCallEntityService entityService,
        LastfmDataSnapshotService dataSnapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService
    ) {
        super(apiCallService, dataSnapshotService, entityService);

        this.attributeSnapshotService = attributeSnapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TAGS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(LastfmArtist entity, LastfmDataSnapshot dataSnapshot) {
        return snapshotAttributes.stream()
            .map(a -> attributeSnapshotService.getOrCreateForEntity(
                dataSnapshot, a.targetEntityType(), a.attribute(), entity))
            .toList();
    }
}
