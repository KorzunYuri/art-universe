package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;
import yurykorzun.art.universe.music.data.raw.lastfm.collectable.common.entity.LastfmEntityType;
import yurykorzun.art.universe.music.data.raw.lastfm.common.LastfmConstants;

@Entity(name = "api_call")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmApiCall extends ApiCall {

    @Id
    @SequenceGenerator(
            name = "api_call_seq_gen",
            sequenceName = "api_call_seq",
            allocationSize = LastfmConstants.HIBERNATE_BATCH_SIZE
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_call_seq_gen")
    private long id;

    @Column(name = "entity_type")
    private LastfmEntityType entityType;

    @Column(name = "entity_id")
    private long entityId;

    // TODO write equals and hashCode when the fieldset is stable
}
