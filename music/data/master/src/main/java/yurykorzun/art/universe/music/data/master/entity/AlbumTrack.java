package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "album_track")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AlbumTrack extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "album_track_seq_gen",
        sequenceName = "album_track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_track_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

    @NonNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", insertable = false, updatable = false)
    private Track track;

    @Column(name = "relation_type_id")
    private Long relationTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_type_id", insertable = false, updatable = false)
    private RelationType relationType;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ALBUM;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public Long getFirstEntityId() {
        return albumId;
    }

    @Override
    public Long getSecondEntityId() {
        return trackId;
    }

    @Override
    public boolean supportsRelationTypes() {
        return true;
    }
}
