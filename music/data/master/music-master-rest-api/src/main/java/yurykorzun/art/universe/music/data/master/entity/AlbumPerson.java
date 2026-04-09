package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.MasterEntityType;
import yurykorzun.art.universe.common.domain.entity.EntityType;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "album_person")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AlbumPerson extends MasterBaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "album_person_seq_gen",
        sequenceName = "album_person_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_person_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

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
        return MasterEntityType.ALBUM;
    }

    @Override
    public EntityType getSecondEntityType() {
        return MasterEntityType.PERSON;
    }

    @Override
    public Long getFirstEntityId() {
        return albumId;
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
