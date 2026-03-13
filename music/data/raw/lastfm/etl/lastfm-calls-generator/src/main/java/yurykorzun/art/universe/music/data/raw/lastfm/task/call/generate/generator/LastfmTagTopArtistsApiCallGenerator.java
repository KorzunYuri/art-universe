package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmTagApiCallGenerator;

import java.util.List;

@Component
@Slf4j
public class LastfmTagTopArtistsApiCallGenerator extends LastfmTagApiCallGenerator {

    private final LastfmAttributeSnapshotService attributeSnapshotService;
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmTagTopArtistsApiCallGenerator(
        LastfmApiCallEntityService entityService,
        LastfmApiCallService apiCallService,
        LastfmDataSnapshotService snapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(apiCallService, snapshotService, entityService);
        this.attributeSnapshotService = attributeSnapshotService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_TAG_TOP_ARTISTS);
    }

    @Override
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(LastfmTag tag, LastfmDataSnapshot dataSnapshot) {
        LastfmAttributeSnapshot rankAttrSnapshot = attributeSnapshotService.getOrCreateForEntity(
            dataSnapshot, LastfmEntityType.ARTIST, LastfmAttribute.RANK, tag);
        return List.of(rankAttrSnapshot);
    }
}
