package yurykorzun.art.universe.music.data.raw.lastfm.task.response.process.mapping;

public enum EntityMappingStage {
    NOT_INITIALIZED(1),
    INITIALIZED(2),
    ENTITY_SAVED(3);

    private final int order;

    EntityMappingStage(int order) {
        this.order = order;
    }
}
