package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "category")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Category extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "category_seq_gen",
        sequenceName = "category_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "category_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "dimension_id")
    private Long dimensionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dimension_id", insertable = false, updatable = false)
    private Dimension dimension;

    @Column(name = "parent_id")
    private Long parentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private Category parent;
}
