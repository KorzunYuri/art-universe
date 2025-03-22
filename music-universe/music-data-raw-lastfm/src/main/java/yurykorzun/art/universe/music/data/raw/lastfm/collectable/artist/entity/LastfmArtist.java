package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.entity.CollectableEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

@Entity(name = "artist")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmArtist extends BaseLastfmEntity {

    @Id
    @SequenceGenerator(
            name = "artist_seq_gen",
            sequenceName = "artist_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq_gen")
    private long id;

    private String mbid;

    private String url;

    @Override
    public CollectableEntityType getType() {
        return LastfmEntityType.ARTIST;
    }
}
