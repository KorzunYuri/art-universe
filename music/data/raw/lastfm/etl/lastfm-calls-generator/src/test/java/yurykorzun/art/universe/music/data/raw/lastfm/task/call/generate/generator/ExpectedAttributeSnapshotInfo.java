package yurykorzun.art.universe.music.data.raw.lastfm.task.call.generate.generator;

import lombok.AllArgsConstructor;
import lombok.Data;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.attribute.LastfmAttribute;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;

@Data
@AllArgsConstructor
public class ExpectedAttributeSnapshotInfo {
    LastfmAttribute attribute;
    LastfmEntityType targetEntityType;
}
