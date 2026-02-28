package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationBindingEntity;

@Entity(name = "artist_album_binding")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistAlbumBinding extends BaseEntity implements RelationBindingEntity {

    @Id
    @SequenceGenerator(
        name = "artist_album_binding_seq_gen",
        sequenceName = "artist_album_binding_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_album_binding_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "master_id", nullable = false)
    private Long masterBindingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", insertable = false, updatable = false)
    private ArtistAlbum artistAlbum;

    @NonNull
    @Column(name = "data_source_id", nullable = false)
    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;

    @NonNull
    @Column(name = "external_artist_id", nullable = false)
    private Long externalArtistId;

    @NonNull
    @Column(name = "external_album_id", nullable = false)
    private Long externalAlbumId;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ARTIST;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.ALBUM;
    }

    @Override
    public Long getExternalFirstEntityId() {
        return externalArtistId;
    }

    @Override
    public Long getExternalSecondEntityId() {
        return externalAlbumId;
    }
}
