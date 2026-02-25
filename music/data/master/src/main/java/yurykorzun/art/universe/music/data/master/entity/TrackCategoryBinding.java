package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationBindingEntity;

@Entity(name = "track_category_binding")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class TrackCategoryBinding extends BaseEntity implements RelationBindingEntity {

    @Id
    @SequenceGenerator(
        name = "track_category_binding_seq_gen",
        sequenceName = "track_category_binding_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_category_binding_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "master_id", nullable = false)
    private Long masterBindingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", insertable = false, updatable = false)
    private TrackCategory trackCategory;

    @NonNull
    @Column(name = "data_source_id", nullable = false)
    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;

    @NonNull
    @Column(name = "external_track_id", nullable = false)
    private Long externalTrackId;

    @NonNull
    @Column(name = "external_category_id", nullable = false)
    private Long externalCategoryId;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.CATEGORY;
    }

    @Override
    public Long getExternalFirstEntityId() {
        return externalTrackId;
    }

    @Override
    public Long getExternalSecondEntityId() {
        return externalCategoryId;
    }
}
