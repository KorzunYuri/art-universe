package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.entity.BaseCollectableEntity;

import jakarta.persistence.*;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;

import java.util.Objects;

/**
 * Base class for Lastfm collected entities. Child classes must provide their own id
 */
@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseLastfmEntity extends BaseCollectableEntity {

    // TODO drop sequences created automatically
    public abstract long getId();

    @Column(name = "name")
    private String name;

    @NonNull
    @JoinColumn(name = "api_call_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LastfmApiCall apiCall;

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
        if (this.getId() != 0) {
            return Long.hashCode(this.getId());
        }
        return Objects.hash(this.getName());
    }
}
