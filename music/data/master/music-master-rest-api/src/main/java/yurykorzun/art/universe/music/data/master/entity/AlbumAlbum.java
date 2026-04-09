package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "album_album")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AlbumAlbum extends MasterBaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "album_album_seq_gen",
        sequenceName = "album_album_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_album_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "source_album_id", nullable = false)
    private Long sourceAlbumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_album_id", insertable = false, updatable = false)
    private Album sourceAlbum;

    @NonNull
    @Column(name = "target_album_id", nullable = false)
    private Long targetAlbumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_album_id", insertable = false, updatable = false)
    private Album targetAlbum;

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
        return MasterEntityType.ALBUM;
    }

    @Override
    public Long getFirstEntityId() {
        return sourceAlbumId;
    }

    @Override
    public Long getSecondEntityId() {
        return targetAlbumId;
    }

    @Override
    public boolean supportsRelationTypes() {
        return true;
    }
}
