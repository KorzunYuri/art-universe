package yurykorzun.art.universe.music.data.raw.spotify.domain.entity.common;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.spotify.enums.SpotifyEntityType;

import java.util.Objects;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseSpotifyEntity extends BaseSpotifyCollectable {

    public abstract long getId();

    @Transient
    public abstract SpotifyEntityType getEntityType();

    @Column(name = "spotify_id")
    protected String spotifyId;

    @Column(name = "name")
    protected String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseSpotifyEntity other)) return false;
        if (!other.getEntityType().equals(getEntityType())) return false;
        if (this.getId() != 0 && other.getId() != 0) {
            return this.getId() == other.getId();
        }
        return Objects.equals(this.getSpotifyId(), other.getSpotifyId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getSpotifyId(), this.getEntityType());
    }
}
