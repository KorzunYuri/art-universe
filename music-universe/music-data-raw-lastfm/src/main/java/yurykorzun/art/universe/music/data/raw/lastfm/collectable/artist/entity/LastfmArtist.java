package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.Objects;

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

    @Setter
    private String mbid;

    @Setter
    private String url;

    @Override
    public LastfmEntityType getType() {
        return LastfmEntityType.ARTIST;
    }

    @Override
    public String getUniqueKey() {
        return getName();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LastfmArtist artist)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(mbid, artist.mbid) && Objects.equals(url, artist.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mbid, url);
    }
}
