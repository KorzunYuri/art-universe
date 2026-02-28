package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.art.data.models.entity.ArtEntityType;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "artist_person")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistPerson extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "artist_person_seq_gen",
        sequenceName = "artist_person_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_person_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    private Artist artist;

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
        return MasterEntityType.ARTIST;
    }

    @Override
    public EntityType getSecondEntityType() {
        return ArtEntityType.PERSON;
    }

    @Override
    public Long getFirstEntityId() {
        return artistId;
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
