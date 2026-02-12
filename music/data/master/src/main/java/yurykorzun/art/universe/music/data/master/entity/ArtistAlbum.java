package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "artist_album")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistAlbum extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "artist_album_seq_gen",
        sequenceName = "artist_album_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_album_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    private Artist artist;

    @NonNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

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
        return MasterEntityType.ALBUM;
    }

    @Override
    public Long getFirstEntityId() {
        return artistId;
    }

    @Override
    public Long getSecondEntityId() {
        return albumId;
    }

    @Override
    public boolean supportsRelationTypes() {
        return true;
    }
}
