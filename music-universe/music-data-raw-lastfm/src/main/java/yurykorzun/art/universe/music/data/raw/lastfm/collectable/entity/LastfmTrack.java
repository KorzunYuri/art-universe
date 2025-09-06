package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.dto.ArtistScoped;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.util.Objects;

@Entity(name = "track")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class LastfmTrack extends BaseLastfmEntity implements ArtistScoped {

    @Id
    @SequenceGenerator(
        name = "track_seq_gen",
        sequenceName = "track_seq",
        allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
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

    @Column(name = "listeners_count")
    private Integer listenersCount;

    @Column(name = "play_count")
    private Long playCount;
    
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id")
    private LastfmArtist artist;

    @Override
    public LastfmEntityType getType() {
        return LastfmEntityType.TRACK;
    }

    @Override
    public String getArtistName() {
        return artist != null ? artist.getName() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LastfmTrack that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(duration,     that.duration)
            && Objects.equals(mbid,         that.mbid)
            && Objects.equals(url,          that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), duration, mbid, url);
    }

    @Override
    public String getUniqueKey() {
        return String.format("%s|%s|%s", name, mbid, url);
    }
}
