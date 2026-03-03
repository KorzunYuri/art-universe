package yurykorzun.art.universe.music.data.raw.spotify.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.common.SpotifyConstants;
import yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common.BaseSpotifyEntity;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

@Entity(name = "genre")
@SuperBuilder
@NoArgsConstructor
@Getter @Setter
public class SpotifyGenre extends BaseSpotifyEntity {

    @Id
    @SequenceGenerator(name = "genre_seq_gen", sequenceName = "genre_seq", allocationSize = SpotifyConstants.HIBERNATE_BATCH_SIZE)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "genre_seq_gen")
    @Setter(AccessLevel.NONE)
    private long id;

    @Override
    public SpotifyEntityType getEntityType() {
        return SpotifyEntityType.GENRE;
    }
}
