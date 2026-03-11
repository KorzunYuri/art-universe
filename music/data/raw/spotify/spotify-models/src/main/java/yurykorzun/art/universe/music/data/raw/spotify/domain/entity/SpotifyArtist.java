package yurykorzun.art.universe.music.data.raw.spotify.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyEntity;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

@Entity(name = "artist")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class SpotifyArtist extends BaseSpotifyEntity {

    @Id
    @SequenceGenerator(name = "artist_seq_gen", sequenceName = "artist_seq", allocationSize = SpotifyConstants.JDBC_ENTITY_SEQ_ALLOCATION)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "artist_seq_gen")
    @Setter(AccessLevel.NONE)
    private long id;

    @Column(name = "spotify_url")
    private String spotifyUrl;

    @Column(name = "uri")
    private String uri;

    @Override
    public SpotifyEntityType getEntityType() {
        return SpotifyEntityType.ARTIST;
    }
}
