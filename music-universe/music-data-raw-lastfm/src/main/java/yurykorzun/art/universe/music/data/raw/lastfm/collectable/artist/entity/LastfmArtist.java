package yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
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
@Getter @Setter
public class LastfmArtist extends BaseLastfmEntity {

    @Id
    @SequenceGenerator(
            name = "artist_seq_gen",
            sequenceName = "artist_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private long id;

    @Column(name = "mbid")
    private String mbid;

    @Column(name = "url")
    private String url;

    @Deprecated
    @Column(name = "is_streamable")
    private Boolean isStreamable;

    @Deprecated
    @Column(name = "is_on_tour")
    private Boolean isOnTour;

    @Column(name = "listeners_count")
    private Integer listenersCount;

    @Column(name = "play_count")
    private Long playCount;

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
