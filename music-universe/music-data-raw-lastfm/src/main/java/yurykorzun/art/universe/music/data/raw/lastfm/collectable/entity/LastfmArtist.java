package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

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

    @Column(name = "listeners_count")
    private Integer listenersCount;

    @Column(name = "play_count")
    private Long playCount;

    /**
     * A field to distinguish a 'canonical' artist from the artists with the same mbid
     */
    @Builder.Default
    @Column(name = "is_primary")
    private Boolean isPrimary = false;

    @Override
    public LastfmEntityType getType() {
        return LastfmEntityType.ARTIST;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LastfmArtist artist)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(mbid, artist.mbid)
            && Objects.equals(url, artist.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mbid, url);
    }

    @Override
    public String getUniqueKey() {
        return name;
    }
}
