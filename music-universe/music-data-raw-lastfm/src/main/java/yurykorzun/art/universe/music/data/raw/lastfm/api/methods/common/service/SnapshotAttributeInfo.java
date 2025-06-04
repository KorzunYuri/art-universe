package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.service;

import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

public record SnapshotAttributeInfo(LastfmAttribute attribute, LastfmEntityType targetEntityType) {
}
