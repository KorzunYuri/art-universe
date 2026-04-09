package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.entity.MasterBaseEntity;

@Entity(name = "category_category")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class CategoryCategory extends MasterBaseEntity {

    @Id
    @SequenceGenerator(
        name = "category_category_seq_gen",
        sequenceName = "category_category_seq",
        allocationSize = 10
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_category_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "source_category_id", nullable = false)
    private Long sourceCategoryId;

    @NonNull
    @Column(name = "target_category_id", nullable = false)
    private Long targetCategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_category_id", insertable = false, updatable = false)
    private Category sourceCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_category_id", insertable = false, updatable = false)
    private Category targetCategory;
}
