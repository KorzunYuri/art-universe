package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.CollectableEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.album.entity.LastfmAlbum;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.artist.entity.LastfmArtist;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.track.entity.LastfmTrack;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmSpecific;

import java.util.Arrays;

@Getter
public enum LastfmEntityType implements LastfmSpecific, Coded, CollectableEntityType {

    ARTIST(1,   LastfmArtist.class),
    ALBUM(2,    LastfmAlbum.class),
    TRACK(3,    LastfmTrack.class),
    TAG(4,      LastfmTag.class);

    private final int code;
    private final Class<? extends BaseLastfmEntity> entityClass;

    LastfmEntityType(int code, Class<? extends BaseLastfmEntity> entityClass) {
        this.code = code;
        this.entityClass = entityClass;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getName() {
        return name();
    }

    @Override
    public String getTypeName() {
        return "entity_type";
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), LastfmEntityType.class);
    }
}
