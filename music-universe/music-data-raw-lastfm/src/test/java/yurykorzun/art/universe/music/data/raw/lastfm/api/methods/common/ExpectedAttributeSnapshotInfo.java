package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.attribute.entity.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;

@Data
@AllArgsConstructor
public class ExpectedAttributeSnapshotInfo {
    LastfmAttribute attribute;
    LastfmEntityType targetEntityType;
}
