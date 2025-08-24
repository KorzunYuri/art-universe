package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.antlr.v4.runtime.misc.NotNull;
import yurykorzun.art.universe.common.persistence.converter.GzipBase64StringConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

import java.time.LocalDateTime;

@Entity(name = "album")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class LastfmAlbum extends BaseLastfmEntity {

    @Id
    @SequenceGenerator(
        name = "album_seq_gen",
        sequenceName = "album_seq",
        allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_seq_gen")
    @Setter(value = AccessLevel.NONE)
    private long id;

    @Column(name = "mbid")
    private String mbid;

    @NotNull
    @Column(name = "url")
    private String url;

    @Column(name = "play_count")
    private Long playCount;

    @Column(name = "listeners_count")
    private Integer listenersCount;

    @Column(name = "publish_ts")
    private LocalDateTime publishTs;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "artist_id")
    private LastfmArtist artist;

    @Deprecated
    @Column(name = "description")
    @Convert(converter = GzipBase64StringConverter.class)
    private String description;

    @Override
    public LastfmEntityType getType() {
        return LastfmEntityType.ALBUM;
    }

    @Override
    public String getUniqueKey() {
        return String.format("%s|%s|%s", name, mbid, url);
    }
}
