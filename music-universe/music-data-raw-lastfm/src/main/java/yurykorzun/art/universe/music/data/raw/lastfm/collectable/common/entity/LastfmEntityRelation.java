package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

@Entity(name = "entity_relation")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmEntityRelation extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "entity_relation_seq_gen",
            sequenceName = "entity_relation_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_relation_seq_gen")
    private long id;

    @NonNull
    @Column(name = "scope_entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType scopeEntityType;

    @NonNull
    @Column(name = "scope_entity_id")
    private long scopeEntityId;

    @NonNull
    @Column(name = "entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityType;

    @NonNull
    @Column(name = "entity_id")
    private long entityId;

    @NonNull
    @JoinColumn(name = "api_call_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LastfmApiCall apiCall;

}
