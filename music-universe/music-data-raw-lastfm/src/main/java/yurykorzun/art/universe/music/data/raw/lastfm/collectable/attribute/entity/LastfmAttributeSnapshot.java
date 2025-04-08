package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmDataSnapshot;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

@Entity(name = "attribute_snapshot")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmAttributeSnapshot extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "attribute_snapshot_seq_gen",
            sequenceName = "attribute_snapshot_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_snapshot_seq_gen")
    private long id;

    @NonNull
    @Column(name = "attribute_id")
    @Convert(converter = LastfmAttributeConverter.class)
    private LastfmAttribute attribute;

    @NonNull
    @Column(name = "entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityType;

    @Column(name = "scope_entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType scopeEntityType;

    @Column(name = "scope_entity_id")
    private Long scopeEntityId;

    @Column(name = "data_snapshot_id_prev")
    private Long previousSnapshotId;

    @NonNull
    @Column(name = "data_snapshot_id_cur")
    private long currentSnapshotId;

    public void setNewSnapshot(LastfmDataSnapshot snapshot) {
        this.previousSnapshotId = currentSnapshotId;
        this.currentSnapshotId = snapshot.getId();
    }
}
