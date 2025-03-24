package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.topartists.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.service.LastfmEntityService;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.tag.entity.LastfmTag;

import java.util.List;
import java.util.Map;

@Component
public class LastfmTagTopArtistsApiCallGenerator extends LastfmApiCallGenerator {

    private final LastfmEntityService entityService;

    @Value("${lastfm.client.methods.tag.topArtists.dueDurationDays}")
    private int dueDurationDays;

    public LastfmTagTopArtistsApiCallGenerator(LastfmEntityService entityService, LastfmApiCallService apiCallService) {
        super(apiCallService);
        this.entityService = entityService;
    }

    @Override
    public LastfmApiCallType getType() {
        return LastfmApiCallType.TAG_TOP_ARTISTS;
    }

    @Override
    public List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {

        List<LastfmTag> unprocessed = entityService.findAllUnprocessed(LastfmEntityType.TAG, LastfmApiCallType.TAG_TOP_ARTISTS);

        return unprocessed.stream()
                .map(this::artistToApiCallCreateRequest)
            .toList();
    }

    private LastfmApiCallCreateRequest artistToApiCallCreateRequest(LastfmTag tag) {
        return LastfmApiCallCreateRequest.builder()
                .type(getType())
                .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
                .params(generateApiCallParameters(tag))
            .build();
    }

    private Map<String, String> generateApiCallParameters(LastfmTag tag) {
        return Map.of(LastfmApiConstants.PARAM_NAME_TAG, tag.getName());
    }
}
