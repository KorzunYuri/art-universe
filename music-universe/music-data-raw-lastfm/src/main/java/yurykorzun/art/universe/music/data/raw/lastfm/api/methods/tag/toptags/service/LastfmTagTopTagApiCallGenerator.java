package yurykorzun.art.universe.music.data.raw.lastfm.api.methods.tag.toptags.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.dto.LastfmApiCallCreateRequest;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity.LastfmApiCallType;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.repository.LastfmApiCallRepository;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallGenerator;
import yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants;
import yurykorzun.art.universe.music.data.raw.lastfm.api.client.service.LastfmApiCallService;
import yurykorzun.art.universe.music.data.raw.lastfm.api.methods.common.utils.TimeUtil;

import java.util.*;

import static yurykorzun.art.universe.music.data.raw.lastfm.api.LastfmApiConstants.*;

@Component
public class LastfmTagTopTagApiCallGenerator extends LastfmApiCallGenerator {

    private final LastfmApiCallRepository repository;

    @Value("${lastfm.client.methods.tag.topTags.recordsLimit}")
    private int recordsLimit;
    @Value("${lastfm.client.methods.tag.topTags.dueDurationDays}")
    private int dueDurationDays;

    public LastfmTagTopTagApiCallGenerator(LastfmApiCallRepository repository, LastfmApiCallService apiCallService) {
        super(apiCallService);
        this.repository = repository;
    }

    @Override
    public LastfmApiCallType getType() {
        return LastfmApiCallType.TAG_TOP_TAGS;
    }

    @Override
    public List<LastfmApiCallCreateRequest> generateApiCallCreationRequests() {

        //  TODO get pages number from the last api call (@attr.count field)
        final int tagsCount = Integer.MAX_VALUE;
        final int pagesNumber = Math.min(tagsCount, recordsLimit / PAGE_SIZE);

        //  find all pending non expired requests to not duplicate them
        List<LastfmApiCall> pendingCalls = repository.findAllUnexpiredByType(getType());
        Set<Integer> pendingOffsets = new HashSet<>();
        for (LastfmApiCall pendingCall : pendingCalls) {
            pendingOffsets.add(Integer.parseInt(pendingCall.getParams().getOrDefault(LastfmApiConstants.PARAM_NAME_OFFSET, "0")));
        }

        List<LastfmApiCallCreateRequest> calls = new ArrayList<>();
        for (int i = 0; i < pagesNumber; i++) {
            // skip duplicate call
            int offset = i * PAGE_SIZE;
            if (pendingOffsets.contains(offset)) {
                continue;
            }

            // generate new call
            Map<String, String> params = new HashMap<>();
            params.put(LastfmApiConstants.PARAM_NAME_OFFSET, String.valueOf(offset));
            calls.add(LastfmApiCallCreateRequest.builder()
                    .type(getType())
                    .dueDttm(TimeUtil.calcDueDttm(dueDurationDays))
                    .params(params)
                .build());
        }

        return calls;
    }
}
