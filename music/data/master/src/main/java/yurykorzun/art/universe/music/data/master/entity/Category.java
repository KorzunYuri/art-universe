package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import java.util.List;

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

    @OneToMany(mappedBy = "sourceCategory", fetch = FetchType.LAZY)
    private List<CategoryCategory> childRelations;

    @OneToMany(mappedBy = "targetCategory", fetch = FetchType.LAZY)
    private List<CategoryCategory> parentRelations;
}
