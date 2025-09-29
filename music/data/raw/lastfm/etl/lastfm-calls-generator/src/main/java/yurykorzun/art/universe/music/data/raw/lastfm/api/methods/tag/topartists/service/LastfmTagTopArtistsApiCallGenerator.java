package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.common.service.LastfmTagApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.service.attribute.LastfmAttributeSnapshotService;

import java.util.List;

@Component
@Slf4j
public class LastfmTagTopArtistsApiCallGenerator extends LastfmTagApiCallGenerator {

    private final LastfmAttributeSnapshotService attributeSnapshotService;

    @Value("${lastfm.client.methods.tag.topArtists.dueDurationDays}")
    private int dueDurationDays;

    public LastfmTagTopArtistsApiCallGenerator(
            LastfmApiCallEntityService entityService,
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
