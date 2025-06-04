package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service.LastfmTagApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;

@Component
@Slf4j
public class LastfmTagTopArtistsApiCallGenerator extends LastfmTagApiCallGenerator {

    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.client.methods.tag.topArtists.dueDurationDays}")
    private int dueDurationDays;

    public LastfmTagTopArtistsApiCallGenerator(
            LastfmEntityService entityService,
            LastfmApiCallService apiCallService,
            LastfmDataSnapshotService snapshotService,
            LastfmAttributeSnapshotService attributeSnapshotService
    ) {
        super(apiCallService, snapshotService, entityService);

        this.attributeSnapshotService = attributeSnapshotService;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    protected int getDueDurationDays() {
        return dueDurationDays;
    }

    @Override
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(LastfmTag tag, LastfmDataSnapshot dataSnapshot) {
        LastfmAttributeSnapshot rankAttrSnapshot = attributeSnapshotService.getOrCreateForEntity(
            dataSnapshot, LastfmEntityType.ARTIST, LastfmAttribute.RANK, tag);
        return List.of(rankAttrSnapshot);
    }

}
