package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.processing;

public enum EntityMappingStage {
    NOT_INITIALIZED(1),
    INITIALIZED(2),
    ENTITY_SAVED(3);

    private final int order;

    EntityMappingStage(int order) {
        this.order = order;
    }

    boolean isAtLeast(EntityMappingStage targetStage) {
        return this.order >= targetStage.order;
    }
}
