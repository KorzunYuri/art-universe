package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.entity.MasterBaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "artist_artist")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistArtist extends MasterBaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "artist_artist_seq_gen",
        sequenceName = "artist_artist_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_artist_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "source_artist_id", nullable = false)
    private Long sourceArtistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_artist_id", insertable = false, updatable = false)
    private Artist sourceArtist;

    @NonNull
    @Column(name = "target_artist_id", nullable = false)
    private Long targetArtistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_artist_id", insertable = false, updatable = false)
    private Artist targetArtist;

    @Column(name = "relation_type_id")
    private Long relationTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_type_id", insertable = false, updatable = false)
    private RelationType relationType;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ARTIST;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.ARTIST;
    }

    @Override
    public Long getFirstEntityId() {
        return sourceArtistId;
    }

    @Override
    public Long getSecondEntityId() {
        return targetArtistId;
    }

    @Override
    public boolean supportsRelationTypes() {
        return true;
    }
}
