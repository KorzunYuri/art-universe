package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

@Entity(name = "blacklist_entity_url")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class BlacklistedEntityUrl extends BaseEntity {

    @Id
    @SequenceGenerator(
            name = "blacklist_entity_url_seq_gen",
            sequenceName = "blacklist_entity_url_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "blacklist_entity_url_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private Long id;

    @Column(name = "entity_type", nullable = false)
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityType;

    @Column(name = "url", nullable = false, length = 8192)
    private String url;
}
