package yurykorzun.art.universe.music.data.raw.lastfm.domain.event;

import yurykorzun.art.universe.data.raw.common.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

public record EntityStatusChangedEvent(LastfmEntityType entityType, Long entityId, ApprovalStatus newStatus) {
}
