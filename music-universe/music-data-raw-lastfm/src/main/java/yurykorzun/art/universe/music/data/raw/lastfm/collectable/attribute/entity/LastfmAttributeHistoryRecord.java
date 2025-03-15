package yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity;

import jakarta.persistence.*;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityTypeConverter;

import java.time.Instant;

@Entity(name = "attribute_history")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class LastfmAttributeHistoryRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NotNull
    @Column(name = "entity_type_id")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityTypeId;

    @NotNull
    @Column(name = "entity_id")
    private long entityId;

    @NonNull
    @Column(name = "attribute_id")
    @Convert(converter = LastfmAttributeConverter.class)
    private LastfmAttribute attribute;

    @NonNull
    @Builder.Default
    @Column(name = "collection_ts")
    private Instant collectionTs = Instant.now();

    @Column(name = "string_value")
    private String stringValue;

    @Column(name = "int_value")
    private Integer intValue;

}
