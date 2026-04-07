package yurykorzun.art.universe.music.data.raw.lastfm.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.domain.entity.BaseEntity;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.LastfmEntityTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentType;
import yurykorzun.art.universe.music.data.raw.lastfm.domain.entity.common.TextContentTypeConverter;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.etl.entity.LastfmDataSnapshot;

@Entity(name = "entity_text_content")
@SuperBuilder
@NoArgsConstructor
@Getter
public class EntityTextContent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @NonNull
    @Column(name = "entity_type")
    @Convert(converter = LastfmEntityTypeConverter.class)
    private LastfmEntityType entityType;

    @Column(name = "entity_id")
    private long entityId;

    @NonNull
    @Column(name = "content_type")
    @Convert(converter = TextContentTypeConverter.class)
    private TextContentType contentType;

    @Setter
    @NonNull
    @Column(name = "content")
    private String content;

    @Setter
    @Column(name = "published_at")
    private String publishedAt;

    @NonNull
    @JoinColumn(name = "api_call_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LastfmApiCall apiCall;

    @Setter
    @JoinColumn(name = "data_snapshot_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private LastfmDataSnapshot dataSnapshot;
}
