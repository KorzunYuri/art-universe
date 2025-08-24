package yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.music.data.raw.lastfm.common.UniquenessSupport;

import java.util.Objects;

/**
 * Base class for Lastfm collected entities. Child classes must provide their own id
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseLastfmEntity extends BaseLastfmCollectable implements UniquenessSupport {

    // TODO drop sequences created automatically
    public abstract long getId();

    @Transient
    public abstract LastfmEntityType getType();

    @Column(name = "name")
    protected String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BaseLastfmEntity other)) return false;

        if (!other.getType().equals(getType())) return false;

        if (this.getId() != 0 && other.getId() != 0) {
            return this.getId() == other.getId();
        }
        return Objects.equals(this.getName(), other.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getName(), this.getApiCall());
    }
}
