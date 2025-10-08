package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.persistence.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

/**
 * Represents a single request for searching a specific string with artist.search API method.
 */
@Entity(name = "artist_search")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmArtistSearchRequest extends BaseEntity {

    @Id
    @SequenceGenerator(
        name = "artist_search_seq_gen",
        sequenceName = "artist_search_seq",
        allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_search_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private long id;

    @Column(name = "search_string")
    private String searchString;

    @Column(name = "is_processed")
    private boolean processed;

}
