package yurykorzun.art.universe.music.data.master.entity.attribute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.master.model.attribute.AttributeTargetType;

@Entity(name = "attribute_def_applicability")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AttributeDefApplicability {

    @Id
    @SequenceGenerator(
        name = "attribute_def_applicability_seq_gen",
        sequenceName = "attribute_def_applicability_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_def_applicability_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_def_id", nullable = false)
    private AttributeDef attributeDef;

    @NonNull
    @Column(name = "target_type", nullable = false)
    private AttributeTargetType targetType;
}
