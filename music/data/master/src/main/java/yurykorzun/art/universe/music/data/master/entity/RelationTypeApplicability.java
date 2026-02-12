package yurykorzun.art.universe.music.data.master.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;

@Entity(name = "relation_type_applicability")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class RelationTypeApplicability extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "relation_type_applicability_seq_gen",
        sequenceName = "relation_type_applicability_seq",
        allocationSize = 10
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "relation_type_applicability_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "relation_type_id", nullable = false)
    private Long relationTypeId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "relation_type_id", insertable = false, updatable = false)
    private RelationType relationType;

    @NonNull
    @Column(name = "source_entity_type", nullable = false)
    private MasterEntityType sourceEntityType;

    @NonNull
    @Column(name = "target_entity_type", nullable = false)
    private MasterEntityType targetEntityType;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault;
}
