package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "album_category")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AlbumCategory extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "album_category_seq_gen",
        sequenceName = "album_category_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_category_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "album_id", nullable = false)
    private Long albumId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "album_id", insertable = false, updatable = false)
    private Album album;

    @NonNull
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.ALBUM;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.CATEGORY;
    }

    @Override
    public Long getFirstEntityId() {
        return albumId;
    }

    @Override
    public Long getSecondEntityId() {
        return categoryId;
    }
}
