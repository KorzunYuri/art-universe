package yurykorzun.art.universe.music.data.raw.lastfm.domain.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.repository.EntityTextContentRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.service.EntityTextContentService;

@Service
@RequiredArgsConstructor
public class EntityTextContentServiceImpl implements EntityTextContentService {

    private final EntityTextContentRepository repository;

    @Override
    @Transactional
    public void saveTextContent(LastfmEntityType entityType, long entityId,
                                TextContentType contentType, String content, String publishedAt,
                                long apiCallId, Long dataSnapshotId) {
        if (content == null || content.isBlank()) {
            return;
        }

        repository.upsert(
                entityType.getCode(),
                entityId,
                contentType.getCode(),
                content,
                publishedAt,
                apiCallId,
                dataSnapshotId
        );
    }
}
