package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    @Value("${lastfm.tasks.calls-generate.due-duration-days.tag-top-artists}")
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
