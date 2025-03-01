package yurykorzun.art.universe.music.data.raw.lastfm.entity;

import yurykorzun.art.universe.common.persistence.entity.DataCollectionTaskType;
import yurykorzun.art.universe.common.persistence.entity.DataCollectionTaskTypeRegistry;

import java.util.Arrays;

public enum LastfmTaskType implements DataCollectionTaskType {

    TAGS_TOP_TAGS("tags.topTags");

    private final String code;

    LastfmTaskType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return this.code;
    }

    static {
        Arrays.stream(values()).forEach(DataCollectionTaskTypeRegistry::register);
    }
}
