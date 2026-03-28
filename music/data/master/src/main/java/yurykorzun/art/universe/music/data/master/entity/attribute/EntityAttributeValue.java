package yurykorzun.art.universe.music.data.master.entity.attribute;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;

import java.time.LocalDate;

@Entity(name = "entity_attribute_value")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class EntityAttributeValue extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "entity_attribute_value_seq_gen",
        sequenceName = "entity_attribute_value_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_attribute_value_seq_gen")
    @Setter(AccessLevel.NONE)
    private Long id;

    @NonNull
    @Column(name = "target_type", nullable = false)
    private AttributeTargetType targetType;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "attribute_def_id", nullable = false)
    private AttributeDef attributeDef;

    @Column(name = "numeric_value")
    private Long numericValue;

    @Column(name = "string_value", length = 4096)
    private String stringValue;

    @Column(name = "date_value")
    private LocalDate dateValue;

    @Column(name = "bool_value")
    private Boolean boolValue;

    @Column(name = "event_date")
    private LocalDate eventDate;

    @Column(name = "valid_from")
    private LocalDate validFrom;

    @Column(name = "valid_till")
    private LocalDate validTill;

    @NonNull
    @Column(name = "source_type", nullable = false)
    private AttributeSourceType sourceType;

    @Column(name = "source_ref", length = 256)
    private String sourceRef;

    @Column(name = "confidence")
    private Short confidence;

    @Column(name = "origin", nullable = false)
    private Short origin;
}
