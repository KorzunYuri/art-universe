package yurykorzun.art.universe.music.data.raw.lastfm.api.client.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import yurykorzun.art.universe.common.data.raw.api.client.entity.ApiCall;

@Entity(name = "api_call")
@SuperBuilder
@NoArgsConstructor
@Getter
public class LastfmApiCall extends ApiCall {

    @Id
    @SequenceGenerator(
            name = "api_call_seq_gen",
            sequenceName = "api_call_seq",
            allocationSize = 50
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "api_call_seq_gen")
    private long id;

    // TODO write equals and hashCode when the fieldset is stable
}
