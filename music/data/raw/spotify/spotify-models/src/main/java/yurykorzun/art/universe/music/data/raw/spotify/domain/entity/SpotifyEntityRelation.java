package yurykorzun.art.universe.music.data.raw.spotify.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyCollectable;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityTypeConverter;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyRelationTypeConverter;

@Entity(name = "entity_relation")
@SuperBuilder
@NoArgsConstructor
@Getter
public class SpotifyEntityRelation extends BaseSpotifyCollectable {

    @Id
    @SequenceGenerator(name = "entity_relation_seq_gen", sequenceName = "entity_relation_seq", allocationSize = SpotifyConstants.JDBC_ENTITY_SEQ_ALLOCATION)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "entity_relation_seq_gen")
    private long id;

    @Column(name = "source_entity_type")
    @Convert(converter = SpotifyEntityTypeConverter.class)
    private SpotifyEntityType sourceEntityType;

    @Column(name = "source_entity_id")
    private Long sourceEntityId;

    @Column(name = "target_entity_type")
    @Convert(converter = SpotifyEntityTypeConverter.class)
    private SpotifyEntityType targetEntityType;

    @Column(name = "target_entity_id")
    private Long targetEntityId;

    @Column(name = "relation_type")
    @Convert(converter = SpotifyRelationTypeConverter.class)
    private SpotifyRelationType relationType;
}
