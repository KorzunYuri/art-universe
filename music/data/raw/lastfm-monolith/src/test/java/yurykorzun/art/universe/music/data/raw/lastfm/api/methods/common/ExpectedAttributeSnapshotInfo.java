package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.entity.common.LastfmEntityType;

@Data
@AllArgsConstructor
public class ExpectedAttributeSnapshotInfo {
    LastfmAttribute attribute;
    LastfmEntityType targetEntityType;
}
