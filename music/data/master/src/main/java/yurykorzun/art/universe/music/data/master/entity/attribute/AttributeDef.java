package yurykorzun.art.universe.music.data.master.entity.attribute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import java.util.List;

@Entity(name = "attribute_def")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class AttributeDef extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "attribute_def_seq_gen",
        sequenceName = "attribute_def_seq",
        allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_def_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "code", nullable = false, unique = true, length = 64)
    private String code;

    @NonNull
    @Column(name = "name", nullable = false, length = 128)
    private String name;

    @Column(name = "description")
    private String description;

    @NonNull
    @Column(name = "data_type", nullable = false)
    private AttributeDataType dataType;

    @NonNull
    @Column(name = "temporal_type", nullable = false)
    private AttributeTemporalType temporalType;

    @NonNull
    @Column(name = "computation_type", nullable = false)
    private AttributeComputationType computationType;

    @Column(name = "data_source", length = 32)
    private String dataSource;

    @Column(name = "formula")
    private String formula;

    @Column(name = "is_multi_value", nullable = false)
    private boolean multiValue;

    @Column(name = "is_system", nullable = false)
    private boolean system;

    @OneToMany(mappedBy = "attributeDef", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AttributeDefApplicability> applicabilities;
}
