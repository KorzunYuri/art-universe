package yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

import java.util.Objects;

@Entity(name = "track")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class LastfmTrack extends BaseLastfmEntity {

    @Id
    @SequenceGenerator(
        name = "track_seq_gen",
        sequenceName = "track_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private long id;

    @Column(name = "mbid")
    private String mbid;

    @NonNull
    @Column(name = "url")
    private String url;

    @Column(name = "duration")
    private Integer duration;

    @Deprecated
    @Column(name = "is_streamable")
    private Boolean isStreamable;

    @Column(name = "listeners_count")
    private Integer listenersCount;

    @Column(name = "play_count")
    private Long playCount;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private LastfmArtist artist;

    @Override
    public LastfmEntityType getType() {
        return LastfmEntityType.TRACK;
    }

    @Override
    public String getUniqueKey() {
        return getUrl();
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LastfmTrack that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(duration,     that.duration)
            && Objects.equals(isStreamable,   that.isStreamable)
            && Objects.equals(mbid,         that.mbid)
            && Objects.equals(url,          that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), duration, isStreamable, mbid, url);
    }
}
