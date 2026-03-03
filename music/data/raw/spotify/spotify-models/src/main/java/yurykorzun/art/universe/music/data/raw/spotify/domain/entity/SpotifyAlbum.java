package yurykorzun.art.universe.music.data.raw.spotify.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyEntity;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyAlbumType;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyAlbumTypeConverter;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyDatePrecision;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyDatePrecisionConverter;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

@Entity(name = "album")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class SpotifyAlbum extends BaseSpotifyEntity {

    @Id
    @SequenceGenerator(name = "album_seq_gen", sequenceName = "album_seq", allocationSize = SpotifyConstants.HIBERNATE_BATCH_SIZE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "album_seq_gen")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "album_type")
    @Convert(converter = SpotifyAlbumTypeConverter.class)
    private SpotifyAlbumType albumType;

    @Column(name = "total_tracks")
    private Integer totalTracks;

    @Column(name = "release_date")
    private String releaseDate;

    @Column(name = "release_date_precision")
    @Convert(converter = SpotifyDatePrecisionConverter.class)
    private SpotifyDatePrecision releaseDatePrecision;

    @Column(name = "spotify_url")
    private String spotifyUrl;

    @Column(name = "uri")
    private String uri;

    @Column(name = "primary_artist_id")
    private Long primaryArtistId;

    @Column(name = "primary_artist_spotify_id")
    private String primaryArtistSpotifyId;

    @Override
    public SpotifyEntityType getEntityType() {
        return SpotifyEntityType.ALBUM;
    }
}
