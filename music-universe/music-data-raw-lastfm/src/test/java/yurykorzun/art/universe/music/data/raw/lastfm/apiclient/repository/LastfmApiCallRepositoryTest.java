package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import yurykorzun.art.universe.common.data.raw.apiclient.entity.ApiCallStatus;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.apiclient.entity.LastfmApiCallType;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@ActiveProfiles("test")
class LastfmApiCallRepositoryTest {

    @Autowired
    private LastfmApiCallRepository repository;

    @Test
    void testApiCallCreation() {
        LastfmApiCall call = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now())
            .build();

        LastfmApiCall savedCall = repository.save(call);
        assertThat(savedCall.getId()).isNotNull();

        LastfmApiCall foundCall = repository.findById(savedCall.getId()).orElse(null);
        assertThat(foundCall).isNotNull();
        assertThat(foundCall.getType()).isEqualTo(call.getType());
        assertThat(foundCall.getParams()).isEqualTo(call.getParams());
    }

    @Test
    void testApiCallStatusUpdate() {
        LastfmApiCall call = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now())
                .status(ApiCallStatus.CREATED)
            .build();

        LastfmApiCall savedCall = repository.save(call);
        savedCall.setStatus(ApiCallStatus.EXPIRED);
        repository.save(savedCall);

        LastfmApiCall updatedCall = repository.findById(savedCall.getId()).orElse(null);
        assertThat(updatedCall).isNotNull();
        assertThat(updatedCall.getStatus()).isEqualTo(ApiCallStatus.EXPIRED);
    }
}
