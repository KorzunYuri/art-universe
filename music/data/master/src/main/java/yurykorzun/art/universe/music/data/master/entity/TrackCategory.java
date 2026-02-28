package yurykorzun.art.universe.music.data.master.entity;

import yurykorzun.art.universe.common.domain.entity.MasterEntityType;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.master.entity.relation.RelationEntity;

@Entity(name = "track_category")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class TrackCategory extends BaseEntity implements RelationEntity {

    @Id
    @SequenceGenerator(
        name = "track_category_seq_gen",
        sequenceName = "track_category_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_category_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "track_id", nullable = false)
    private Long trackId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "track_id", insertable = false, updatable = false)
    private Track track;

    @NonNull
    @Column(name = "category_id", nullable = false)
    private Long categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, updatable = false)
    private Category category;

    @Override
    public MasterEntityType getFirstEntityType() {
        return MasterEntityType.TRACK;
    }

    @Override
    public MasterEntityType getSecondEntityType() {
        return MasterEntityType.CATEGORY;
    }

    @Override
    public Long getFirstEntityId() {
        return trackId;
    }

    @Override
    public Long getSecondEntityId() {
        return categoryId;
    }
}
