package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.common.config.client.ConfigPropertyHolder;
import yurykorzun.art.universe.music.data.raw.lastfm.config.LastfmGeneratorProperty;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.LastfmApiCallEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmAttributeSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmDataSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.service.LastfmAttributeSnapshotService;
import yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator.common.LastfmArtistApiCallGenerator;

import java.util.List;

@Component
@Slf4j
public class LastfmArtistTopTagsApiCallGenerator extends LastfmArtistApiCallGenerator {

    private static final List<SnapshotAttributeInfo> snapshotAttributes = List.of(
        new SnapshotAttributeInfo(LastfmAttribute.RANK, LastfmEntityType.TAG)
    );

    private final LastfmAttributeSnapshotService attributeSnapshotService;
    private final ConfigPropertyHolder configPropertyHolder;

    public LastfmArtistTopTagsApiCallGenerator(
        LastfmApiCallService apiCallService,
        LastfmApiCallEntityService entityService,
        LastfmDataSnapshotService dataSnapshotService,
        LastfmAttributeSnapshotService attributeSnapshotService,
        ConfigPropertyHolder configPropertyHolder
    ) {
        super(apiCallService, dataSnapshotService, entityService);
        this.attributeSnapshotService = attributeSnapshotService;
        this.configPropertyHolder = configPropertyHolder;
    }

    @Override
    public LastfmApiCallType getApiCallType() {
        return LastfmApiCallType.ARTIST_TOP_TAGS;
    }

    @Override
    protected int getDueDurationDays() {
        return configPropertyHolder.getInt(LastfmGeneratorProperty.DUE_DURATION_ARTIST_TOP_TAGS);
    }

    @Override
    protected List<LastfmAttributeSnapshot> getOrCreateAttributeSnapshots(LastfmArtist entity, LastfmDataSnapshot dataSnapshot) {
        return snapshotAttributes.stream()
            .map(a -> attributeSnapshotService.getOrCreateForEntity(
                dataSnapshot, a.targetEntityType(), a.attribute(), entity))
            .toList();
    }
}
