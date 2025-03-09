package yurykorzun.art.universe.music.data.raw.lastfm.apiclient.repository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;
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
    @Transactional
    void testApiCallCreation() {
        LastfmApiCall created = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now())
            .build();

        LastfmApiCall saved = repository.save(created);
        assertThat(saved).isNotNull();

        LastfmApiCall fetched = repository.getReferenceById(saved.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.getType()).isEqualTo(created.getType());
        assertThat(fetched.getParams()).isEqualTo(created.getParams());
    }

    @Test
    @Transactional
    void testApiCallStatusUpdate() {
        LastfmApiCall created = LastfmApiCall.builder()
                .type(LastfmApiCallType.TAG_TOP_TAGS)
                .params(Map.of("key", "value"))
                .dueDttm(Instant.now())
                .status(ApiCallStatus.CREATED)
            .build();

        LastfmApiCall saved = repository.save(created);
        saved.setStatus(ApiCallStatus.EXPIRED);
        repository.save(saved);

        LastfmApiCall updated = repository.getReferenceById(saved.getId());
        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isEqualTo(ApiCallStatus.EXPIRED);
    }
}
