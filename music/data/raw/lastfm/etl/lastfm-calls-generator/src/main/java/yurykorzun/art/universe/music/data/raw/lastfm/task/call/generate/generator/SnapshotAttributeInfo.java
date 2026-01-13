package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

public record SnapshotAttributeInfo(LastfmAttribute attribute, LastfmEntityType targetEntityType) {
}
