package yurykorzun.art.universe.music.data.raw.lastfm.domain.event;

import yurykorzun.art.universe.common.data.raw.domain.entity.ApprovalStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

public record EntityStatusChangedEvent(LastfmEntityType entityType, Long entityId, ApprovalStatus newStatus) {
}
