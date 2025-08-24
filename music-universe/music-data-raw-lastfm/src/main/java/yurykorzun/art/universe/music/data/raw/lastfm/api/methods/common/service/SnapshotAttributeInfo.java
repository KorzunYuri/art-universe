package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

public record SnapshotAttributeInfo(LastfmAttribute attribute, LastfmEntityType targetEntityType) {
}
