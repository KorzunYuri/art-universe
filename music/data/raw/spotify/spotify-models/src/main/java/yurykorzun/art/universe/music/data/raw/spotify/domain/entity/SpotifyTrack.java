package yurykorzun.art.universe.music.data.raw.spotify.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyEntity;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

@Entity(name = "track")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class SpotifyTrack extends BaseSpotifyEntity {

    @Id
    @SequenceGenerator(name = "track_seq_gen", sequenceName = "track_seq", allocationSize = SpotifyConstants.JDBC_ENTITY_SEQ_ALLOCATION)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "track_seq_gen")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "duration_ms")
    private Integer durationMs;

    @Column(name = "track_number")
    private Integer trackNumber;

    @Column(name = "disc_number")
    private Integer discNumber;

    @Column(name = "has_explicit_lyrics")
    private Boolean hasExplicitLyrics;

    @Column(name = "is_playable")
    private Boolean isPlayable;

    @Column(name = "spotify_url")
    private String spotifyUrl;

    @Column(name = "uri")
    private String uri;

    @Column(name = "isrc")
    private String isrc;

    @Column(name = "ean")
    private String ean;

    @Column(name = "upc")
    private String upc;

    @Column(name = "primary_artist_id")
    private Long primaryArtistId;

    @Column(name = "primary_artist_spotify_id")
    private String primaryArtistSpotifyId;

    @Column(name = "album_id")
    private Long albumId;

    @Column(name = "album_spotify_id")
    private String albumSpotifyId;

    @Override
    public SpotifyEntityType getEntityType() {
        return SpotifyEntityType.TRACK;
    }
}
