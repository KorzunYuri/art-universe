package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "track_person")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class TrackPerson extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "track_person_seq_gen",
        sequenceName = "track_person_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_person_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", insertable = false, updatable = false)
    private Track track;

    @NonNull
    @Column(name = "person_id", nullable = false)
    private Long personId;

    @Column(name = "relation_type_id")
    private Long relationTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_type_id", insertable = false, updatable = false)
    private RelationType relationType;

    @Override
    public EntityType getFirstEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public EntityType getSecondEntityType() {
        return MasterEntityType.PERSON;
    }

    @Override
    public Long getFirstEntityId() {
        return trackId;
    }

    @Override
    public Long getSecondEntityId() {
        return personId;
    }

    @Override
    public boolean supportsRelationTypes() {
        return true;
    }
}
