package yurykorzun.art.universe.music.data.approved.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

@Entity(name = "dimension")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Dimension extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "dimension_seq_gen",
        sequenceName = "dimension_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "dimension_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name", nullable = false)
    private String name;
}
