package yurykorzun.art.universe.music.data.raw.lastfm.domain.service;

import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentType;

public interface EntityTextContentService {

    void saveTextContent(LastfmEntityType entityType, long entityId,
                         TextContentType contentType, String content, String publishedAt,
                         long apiCallId, Long dataSnapshotId);
}
