package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationBindingEntity;

@Entity(name = "album_track_binding")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AlbumTrackBinding extends BaseEntity implements RelationBindingEntity {

    @Id
    @SequenceGenerator(
        name = "album_track_binding_seq_gen",
        sequenceName = "album_track_binding_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_track_binding_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "master_id", nullable = false)
    private Long masterBindingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", insertable = false, updatable = false)
    private AlbumTrack albumTrack;

    @NonNull
    @Column(name = "data_source_id", nullable = false)
    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;

    @NonNull
    @Column(name = "external_album_id", nullable = false)
    private Long externalAlbumId;

    @NonNull
    @Column(name = "external_track_id", nullable = false)
    private Long externalTrackId;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ALBUM;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public Long getExternalFirstEntityId() {
        return externalAlbumId;
    }

    @Override
    public Long getExternalSecondEntityId() {
        return externalTrackId;
    }
}
