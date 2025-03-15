package yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.entity.CollectableEntityType;

import jakarta.persistence.*;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.BaseLastfmEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

@Entity(name = "tag")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmTag extends BaseLastfmEntity {

    @Override
    @Transient
    public CollectableEntityType getType() {
        return LastfmEntityType.TAG;
    }
}
