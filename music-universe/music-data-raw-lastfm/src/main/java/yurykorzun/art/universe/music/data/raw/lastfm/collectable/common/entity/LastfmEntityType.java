package yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity;

import lombok.Getter;
import yurykorzun.art.universe.common.Coded;
import yurykorzun.art.universe.common.CodedRegistry;
import yurykorzun.art.universe.common.data.raw.entity.CollectableEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmSpecific;

import java.util.Arrays;

@Getter
public enum LastfmEntityType implements LastfmSpecific, Coded, CollectableEntityType {

    ARTIST(1),
    ALBUM(2),
    TRACK(3),
    TAG(4);

    private final int code;

    LastfmEntityType(int code) {
        this.code = code;
    }

    @Override
    public Integer getCode() {
        return code;
    }

    @Override
    public String getTypeName() {
        return "entity_type";
    }

    static {
        CodedRegistry.register(Arrays.asList(values()), LastfmEntityType.class);
    }
}
