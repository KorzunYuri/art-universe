package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "artist_category")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ArtistCategory extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "artist_category_seq_gen",
        sequenceName = "artist_category_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_category_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "artist_id", nullable = false)
    private Long artistId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id", insertable = false, updatable = false)
    private Artist artist;

    @NonNull
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;
    
    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ARTIST;
    }
    
    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.CATEGORY;
    }
    
    @Override
    public Long getFirstEntityId() {
        return artistId;
    }
    
    @Override
    public Long getSecondEntityId() {
        return categoryId;
    }
}
