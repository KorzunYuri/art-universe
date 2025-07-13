package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.approved.relation.RelationBindingEntity;

@Entity(name = "artist_category_binding")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistCategoryBinding extends BaseEntity implements RelationBindingEntity {

    @Id
    @SequenceGenerator(
        name = "artist_category_binding_seq_gen",
        sequenceName = "artist_category_binding_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_category_binding_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "reference_id", nullable = false)
    private Long referenceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reference_id", insertable = false, updatable = false)
    private ArtistCategory artistCategory;

    @NonNull
    @Column(name = "data_source_id", nullable = false)
    @Convert(converter = DataSourceConverter.class)
    private DataSource dataSource;

    @NonNull
    @Column(name = "external_artist_id", nullable = false)
    private Long externalArtistId;

    @NonNull
    @Column(name = "external_category_id", nullable = false)
    private Long externalCategoryId;
    
    @Override
    public EntityType getFirstEntityType() {
        return EntityType.ARTIST;
    }
    
    @Override
    public EntityType getSecondEntityType() {
        return EntityType.CATEGORY;
    }
    
    @Override
    public Long getExternalFirstEntityId() {
        return externalArtistId;
    }
    
    @Override
    public Long getExternalSecondEntityId() {
        return externalCategoryId;
    }
}
