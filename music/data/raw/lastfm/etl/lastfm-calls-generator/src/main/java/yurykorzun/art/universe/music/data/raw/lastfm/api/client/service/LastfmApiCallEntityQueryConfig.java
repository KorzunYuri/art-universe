package yurykorzun.art.universe.music.data.raw.lastfm.api.client.service;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

@Builder
@Getter
public class LastfmApiCallEntityQueryConfig {

    @Builder.Default
    private Sort sort = Sort.by(Sort.Direction.ASC, "id");

    @Builder.Default
    private Limit limit = Limit.of(LastfmConstants.HIBERNATE_BATCH_SIZE);

    @Builder.Default
    private boolean approvedEntitiesOnly = true;
}
