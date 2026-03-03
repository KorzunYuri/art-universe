package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "track_track")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class TrackTrack extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "track_track_seq_gen",
        sequenceName = "track_track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_track_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "source_track_id", nullable = false)
    private Long sourceTrackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_track_id", insertable = false, updatable = false)
    private Track sourceTrack;

    @NonNull
    @Column(name = "target_track_id", nullable = false)
    private Long targetTrackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_track_id", insertable = false, updatable = false)
    private Track targetTrack;

    @Column(name = "relation_type_id")
    private Long relationTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_type_id", insertable = false, updatable = false)
    private RelationType relationType;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public Long getFirstEntityId() {
        return sourceTrackId;
    }

    @Override
    public Long getSecondEntityId() {
        return targetTrackId;
    }

    @Override
    public boolean supportsRelationTypes() {
        return true;
    }
}
