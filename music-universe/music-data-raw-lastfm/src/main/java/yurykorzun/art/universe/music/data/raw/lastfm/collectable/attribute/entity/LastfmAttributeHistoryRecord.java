package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

import java.time.Instant;
import java.util.Objects;

@Entity(name = "attribute_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LastfmAttributeHistoryRecord {

    @Id
    @SequenceGenerator(
            name = "attribute_history_seq_gen",
            sequenceName = "attribute_history_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "attribute_history_seq_gen")
    private long id;

    @NonNull
    @Column(name = "api_call_id")
    private long apiCallId;

    @NotNull
    @Column(name = "entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityTypeId;

    @NotNull
    @Column(name = "entity_id")
    private long entityId;

    @NonNull
    @Column(name = "attribute_id")
    @Convert(converter = LastfmAttributeConverter.class)
    private LastfmAttribute attribute;

    @NotNull
    @Column(name = "scope_entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType scopeEntityTypeId;

    @NotNull
    @Column(name = "scope_entity_id")
    private long scopeEntityId;

    @NonNull
    @Builder.Default
    @Column(name = "collection_ts")
    private Instant collectionTs = Instant.now();

    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "int_value")
    private Integer intValue;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LastfmAttributeHistoryRecord other)) return false;

        if (this.getId() != 0 && other.getId() != 0) {
            return this.getId() == other.getId();
        }
        return      entityId == other.entityId
                &&  entityTypeId == other.entityTypeId
                &&  attribute == other.attribute
                &&  Objects.equals(collectionTs, other.collectionTs);
    }

    @Override
    public int hashCode() {
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(entityTypeId, entityId, attribute);
    }

}
