package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

@Entity(name = "relation_type")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class RelationType extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "relation_type_seq_gen",
        sequenceName = "relation_type_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "relation_type_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name", nullable = false, unique = true)
    private String name;

    @Column(name = "reverse_name")
    private String reverseName;

    @Column(name = "is_symmetrical", nullable = false)
    private boolean symmetrical;
}
