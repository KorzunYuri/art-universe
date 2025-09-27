package yurykorzun.art.universe.music.data.raw.lastfm.collectable.event;

import yurykorzun.art.universe.common.data.raw.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;


public record EntityStatusChangedEvent(LastfmEntityType entityType, Long entityId, ApprovalStatus newStatus) {
}
