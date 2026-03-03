package yurykorzun.art.universe.art.data.models.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

@Entity(name = "person")
@Table(schema = "art")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class Person extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "person_seq_gen",
        sequenceName = "person_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "person_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "name")
    private String name;
}
