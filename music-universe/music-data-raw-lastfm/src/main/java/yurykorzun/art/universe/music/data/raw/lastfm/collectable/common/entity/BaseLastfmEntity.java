package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.entity.BaseCollectableEntity;

import jakarta.persistence.*;

import java.util.Objects;

@MappedSuperclass
@SuperBuilder
@NoArgsConstructor
@Getter
public abstract class BaseLastfmEntity extends BaseCollectableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "name")
    private String name;

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
